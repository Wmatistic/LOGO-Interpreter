import java.util.ArrayList;
import java.util.List;

public class Visitor extends logoBaseVisitor<Double> {

    private static final int MAX_DEPTH = 1000;

    private final Engine      engine;
    private final SymbolTable symbols;
    private int               callDepth = 0;

    public Visitor(Engine engine, SymbolTable symbols) {
        this.engine  = engine;
        this.symbols = symbols;
    }

    private static String stripSigil(String s) { return s.substring(1); }

    @Override
    public Double visitInt(logoParser.IntContext ctx) {
        return (double) Integer.parseInt(ctx.INT().getText());
    }

    @Override
    public Double visitFloat(logoParser.FloatContext ctx) {
        return Double.parseDouble(ctx.FLOAT().getText());
    }

    @Override
    public Double visitNegate(logoParser.NegateContext ctx) {
        return -visit(ctx.expr());
    }

    @Override
    public Double visitParen(logoParser.ParenContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Double visitVarRef(logoParser.VarRefContext ctx) {
        String name = stripSigil(ctx.PARAM().getText());
        if (!symbols.hasVar(name))
            throw new RuntimeException("Undefined variable: :" + name);
        return symbols.getVar(name);
    }

    @Override
    public Double visitAddSub(logoParser.AddSubContext ctx) {
        double l = visit(ctx.left);
        double r = visit(ctx.right);
        return ctx.op.getText().equals("+") ? l + r : l - r;
    }

    @Override
    public Double visitMulDiv(logoParser.MulDivContext ctx) {
        double l = visit(ctx.left);
        double r = visit(ctx.right);
        if (ctx.op.getText().equals("/")) {
            if (r == 0) throw new RuntimeException("Division by zero");
            return l / r;
        }
        return l * r;
    }

    @Override
    public Double visitCompare(logoParser.CompareContext ctx) {
        double l = visit(ctx.left);
        double r = visit(ctx.right);
        String op = ctx.op.getText();
        boolean result;
        if      (op.equals(">"))  result = l >  r;
        else if (op.equals("<"))  result = l <  r;
        else if (op.equals(">=")) result = l >= r;
        else if (op.equals("<=")) result = l <= r;
        else if (op.equals("==")) result = l == r;
        else if (op.equals("!=")) result = l != r;
        else throw new RuntimeException("Unknown operator: " + op);
        return result ? 1.0 : 0.0;
    }

    @Override public Double visitFd(logoParser.FdContext ctx) { engine.move( visit(ctx.expr())); return null; }
    @Override public Double visitBk(logoParser.BkContext ctx) { engine.move(-visit(ctx.expr())); return null; }
    @Override public Double visitRt(logoParser.RtContext ctx) { engine.rotate( visit(ctx.expr())); return null; }
    @Override public Double visitLt(logoParser.LtContext ctx) { engine.rotate(-visit(ctx.expr())); return null; }
    @Override public Double visitPu(logoParser.PuContext ctx) { engine.penUp();   return null; }
    @Override public Double visitPd(logoParser.PdContext ctx) { engine.penDown(); return null; }
    @Override public Double visitHm(logoParser.HmContext ctx) { engine.home();    return null; }

    @Override
    public Double visitSc(logoParser.ScContext ctx) {
        engine.setColor((int) visit(ctx.expr()).doubleValue());
        return null;
    }

    @Override
    public Double visitSw(logoParser.SwContext ctx) {
        engine.setStrokeWidth((int) visit(ctx.expr()).doubleValue());
        return null;
    }

    @Override
    public Double visitPc(logoParser.PcContext ctx) {
        List<logoParser.ExprContext> channels = ctx.expr();
        int r = (int) visit(channels.get(0)).doubleValue();
        int g = (int) visit(channels.get(1)).doubleValue();
        int b = (int) visit(channels.get(2)).doubleValue();
        engine.setColor(r, g, b);
        return null;
    }

    @Override
    public Double visitSetx(logoParser.SetxContext ctx) {
        engine.setX(visit(ctx.expr()));
        return null;
    }

    @Override
    public Double visitSety(logoParser.SetyContext ctx) {
        engine.setY(visit(ctx.expr()));
        return null;
    }

    @Override
    public Double visitBf(logoParser.BfContext ctx) {
        engine.beginFill();
        return null;
    }

    @Override
    public Double visitEf(logoParser.EfContext ctx) {
        engine.endFill();
        return null;
    }

    @Override
    public Double visitRepeat(logoParser.RepeatContext ctx) {
        int k = (int) visit(ctx.expr()).doubleValue();
        for (int i = 0; i < k; i++)
            for (var s : ctx.stmt()) visit(s);
        return null;
    }

    @Override
    public Double visitMake(logoParser.MakeContext ctx) {
        String name  = stripSigil(ctx.VAR().getText());
        double value = visit(ctx.expr());
        symbols.setVar(name, value);
        return null;
    }

    @Override
    public Double visitIfStmt(logoParser.IfStmtContext ctx) {
        double cond = visit(ctx.expr());
        List<logoParser.StmtContext> allStmts = ctx.stmt();

        int elseTok = -1;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("else")) { elseTok = i; break; }
        }

        List<logoParser.StmtContext> thenStmts = new ArrayList<>();
        List<logoParser.StmtContext> elseStmts = new ArrayList<>();

        for (logoParser.StmtContext s : allStmts) {
            if (elseTok < 0 || s.getSourceInterval().a < ctx.getChild(elseTok).getSourceInterval().a)
                thenStmts.add(s);
            else
                elseStmts.add(s);
        }

        if (cond != 0) {
            for (var s : thenStmts) visit(s);
        } else {
            for (var s : elseStmts) visit(s);
        }
        return null;
    }

    @Override
    public Double visitWhileStmt(logoParser.WhileStmtContext ctx) {
        while (visit(ctx.expr()) != 0)
            for (var s : ctx.stmt()) visit(s);
        return null;
    }

    @Override
    public Double visitProcDef(logoParser.ProcDefContext ctx) {
        String name = ctx.ID().getText();
        List<String> params = new ArrayList<>();
        for (var p : ctx.PARAM())
            params.add(stripSigil(p.getText()));

        long distinct = params.stream().distinct().count();
        if (distinct != params.size())
            throw new RuntimeException("Duplicate parameter name in procedure: " + name);

        symbols.defProc(new SymbolTable.ProcDef(name, params, ctx));
        return null;
    }

    @Override
    public Double visitProcCall(logoParser.ProcCallContext ctx) {
        String name = ctx.ID().getText();

        if (!symbols.hasProc(name))
            throw new RuntimeException("Undefined procedure: " + name);

        if (callDepth >= MAX_DEPTH)
            throw new RuntimeException("Maximum recursion depth exceeded");

        SymbolTable.ProcDef proc = symbols.getProc(name);
        List<logoParser.ExprContext> args = ctx.expr();

        if (args.size() != proc.params.size())
            throw new RuntimeException(String.format(
                "Procedure '%s' expects %d argument(s) but got %d",
                name, proc.params.size(), args.size()));

        List<Double> argValues = new ArrayList<>();
        for (var a : args) argValues.add(visit(a));

        callDepth++;
        symbols.pushScope();
        for (int i = 0; i < proc.params.size(); i++)
            symbols.setVar(proc.params.get(i), argValues.get(i));

        for (var s : proc.ctx.stmt()) visit(s);

        symbols.popScope();
        callDepth--;
        return null;
    }

    @Override
    public Double visitProg(logoParser.ProgContext ctx) {
        for (var s : ctx.stmt()) visit(s);
        return null;
    }
}
