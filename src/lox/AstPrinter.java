package lox;

import java.util.List;
import java.util.stream.Collectors;

class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    private final List<Stmt> statements;

    public AstPrinter(List<Stmt> statements) {
        this.statements = statements;
    }

    public void printAst() {
        for (Stmt stmt : statements) {
            String output = print(stmt);
            System.out.println(output);
        }
    }

    private String print(Stmt stmt) {
        return stmt.accept(this);
    }

    private String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return expr.name.lexeme + " = " + print(expr.value);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return print(expr.left) + " " + expr.operator.lexeme + " " + print(expr.right);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return print(expr.callee) + "(" + expr.arguments.stream().map(this::print).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public String visitLambdaExpr(Expr.Lambda expr) {
        return "lambda(" + expr.function.params.stream().map(p -> p.lexeme).collect(Collectors.joining(", ")) + ") ";
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return print(expr.object) + "." + expr.name.lexeme;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return "(" + print(expr.expression) + ")";
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        if (expr.value instanceof String) return "\"" + expr.value + "\"";
        return expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return print(expr.left) + " " + expr.operator.lexeme + " " + print(expr.right);
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return print(expr.object) + "." + expr.name.lexeme + " = " + print(expr.value);
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return "super." + expr.method.lexeme;
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "this";
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return expr.operator.lexeme + print(expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");

        for (Stmt statement : stmt.statements) {
            String[] lines = statement.accept(this).split("\n");
            for (String line : lines) {
                builder.append("    ").append(line).append("\n");
            }
        }

        builder.append("}");
        return builder.toString();
    }

    @Override
    public String visitClassStmt(Stmt.Class stmt) {
        return "class " + stmt.name.lexeme + " { }";
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return print(stmt.expression) + ";";
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        return "fun " + stmt.name.lexeme + "(" + stmt.params.stream().map(p -> p.lexeme).collect(Collectors.joining(", ")) + ") { }";
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        return "if (" + print(stmt.condition) + ") " + print(stmt.thenBranch) + (stmt.elseBranch != null ? " else " + print(stmt.elseBranch) : "");
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return "print " + print(stmt.expression) + ";";
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        return "return " + print(stmt.value) + ";";
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        return stmt.varType.toString().toLowerCase() + " " + stmt.name.lexeme + (stmt.initializer != null ? " = " + print(stmt.initializer) : "") + ";";
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return "while (" + print(stmt.condition) + ") " + print(stmt.body);
    }

    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return "break;";
    }

    @Override
    public String visitContinueStmt(Stmt.Continue stmt) {
        return "continue;";
    }
}