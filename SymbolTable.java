import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {

    public static class ProcDef {
        public final String name;
        public final List<String> params;
        public final logoParser.ProcDefContext ctx;

        public ProcDef(String name, List<String> params, logoParser.ProcDefContext ctx) {
            this.name   = name;
            this.params = params;
            this.ctx    = ctx;
        }
    }

    private final java.util.ArrayDeque<Map<String, Double>> scopeStack = new java.util.ArrayDeque<>();
    private final Map<String, ProcDef> procedures = new HashMap<>();

    public SymbolTable() {
        scopeStack.push(new HashMap<>());
    }

    public void pushScope() {
        scopeStack.push(new HashMap<>());
    }

    public void popScope() {
        if (scopeStack.size() <= 1)
            throw new RuntimeException("Internal error: cannot pop global scope");
        scopeStack.pop();
    }

    public void setVar(String name, double value) {
        scopeStack.peek().put(name, value);
    }

    public double getVar(String name) {
        for (Map<String, Double> scope : scopeStack) {
            if (scope.containsKey(name))
                return scope.get(name);
        }
        throw new RuntimeException("Undefined variable: " + name);
    }

    public boolean hasVar(String name) {
        for (Map<String, Double> scope : scopeStack)
            if (scope.containsKey(name)) return true;
        return false;
    }

    public void defProc(ProcDef proc) {
        if (procedures.containsKey(proc.name))
            throw new RuntimeException("Procedure already defined: " + proc.name);
        procedures.put(proc.name, proc);
    }

    public ProcDef getProc(String name) {
        ProcDef p = procedures.get(name);
        if (p == null)
            throw new RuntimeException("Undefined procedure: " + name);
        return p;
    }

    public boolean hasProc(String name) {
        return procedures.containsKey(name);
    }
}
