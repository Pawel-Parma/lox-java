package lox;


class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    int blockDepth = 0;

    String print(Stmt stmt) {
        return stmt.accept(this);
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        blockDepth += 1;
        StringBuilder builder = new StringBuilder("{\n");
        for (Stmt s : stmt.statements) {
            builder.append("  ".repeat(blockDepth));
            builder.append(s.accept(this));
            builder.append("\n");
        }
        blockDepth -= 1;
        builder.append("  ".repeat(blockDepth));
        builder.append("}");
        return builder.toString();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return stmt.expression.accept(this);
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return "(out " + stmt.expression.accept(this) + ")";
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        String value = stmt.initializer == null ? "nil" : stmt.initializer.accept(this);
        return "(new " + stmt.name.lexeme + " = " + value + ")";
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return "(let " + expr.name.lexeme + " = " + expr.value.accept(this) + ")";
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("par", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        if (expr.value instanceof String) return "\"" + expr.value + "\"";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}