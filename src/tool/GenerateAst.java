package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Lambda   : Stmt.Function function",
                "Get      : Expr obj, Token name",
                "Grouping : Expr expression",
                "Literal  : Any? value",
                "Logical  : Expr left, Token operator, Expr right",
                "Set      : Expr obj, Token name, Expr value",
                "Super    : Token keyword, Token method",
                "This     : Token keyword",
                "Unary    : Token operator, Expr right",
                "Variable : Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Import     : Token name, Token? alias",
                "Block      : List<Stmt> statements",
                "Class      : Token name, Expr.Variable? superclass, List<Function> methods",
                "Expression : Expr expression",
                "Function   : Token? name, List<Token> params, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt? elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr? value",
                "Var        : Token name, Expr? initializer, TokenType varType",
                "While      : Expr condition, Stmt body",
                "Break      : Token keyword",
                "Continue   : Token keyword"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".kt";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package lox.tool_gen");
        writer.println();
        writer.println("import lox.token.TokenType");
        writer.println("import lox.token.Token");
        writer.println();
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // The base accept() method.
        writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        fun visit" + typeName + baseName + "(" + baseName.toLowerCase() + ": " + typeName + "): R");
        }
        writer.println("    }");
        writer.println();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.print("    class " + className + "(");

        // Fields.
        String[] fields = fieldList.split(", ");
        int i = 0;
        int len = fields.length;
        for (String field : fields) {
            String[] split = field.split(" ");
            String type = split[0];
            String name = split[1];
            writer.print("@JvmField val " + name + ": " + type + (i < len - 1 ? ", " : ""));
            i += 1;
        }
        writer.println(") : " + baseName + "() {");

        // Visitor pattern.
        writer.println("        override fun <R> accept(visitor: Visitor<R>): R {");
        writer.println("            return visitor.visit" + className + baseName + "(this)");
        writer.println("        }");

        writer.println("    }");
        writer.println();
    }
}
