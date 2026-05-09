import java.io.IOException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Driver {

    public static void main(String[] args) throws IOException {

        final int SIZE = (args.length == 0) ? 700 : Integer.parseInt(args[0]);

        CharStream        input  = CharStreams.fromStream(System.in);
        logoLexer         lexer  = new logoLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        logoParser        parser = new logoParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?,?> recognizer,
                                    Object offendingSymbol,
                                    int line, int charPositionInLine,
                                    String msg, RecognitionException e) {
                System.err.printf("Syntax error at %d:%d — %s%n",
                    line, charPositionInLine, msg);
                System.exit(1);
            }
        });

        logoParser.ProgContext tree = parser.prog();

        Engine      logoEngine = new SVGEngine(SIZE, SIZE);
        SymbolTable symbols    = new SymbolTable();
        Visitor     visitor    = new Visitor(logoEngine, symbols);

        logoEngine.open();
        try {
            visitor.visit(tree);
        } catch (RuntimeException ex) {
            System.err.println("Runtime error: " + ex.getMessage());
            logoEngine.close();
            System.exit(1);
        }
        logoEngine.close();
    }
}
