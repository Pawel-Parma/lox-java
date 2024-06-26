package lox;

import lox.token.Token;
import lox.token.TokenType;
import lox.tool_gen.Stmt;
import lox.tool_gen.Expr;

import java.util.*;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final ModuleInfo moduleInfo;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private final Stack<Set<String>> constants = new Stack<>(); // Add this line
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;
    private boolean isInLoop = false;

    Resolver(Interpreter interpreter, ModuleInfo moduleInfo) {
        this.interpreter = interpreter;
        this.moduleInfo = moduleInfo;
    }

    private enum FunctionType {
        NONE,
        FUNCTION,
        LAMBDA,
        INITIALIZER,
        METHOD
    }

    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    @Override
    public Void visitImportStmt(Stmt.Import stmt) {
        boolean needsAs = false;
        char[] chars = stmt.name.lexeme.toCharArray();
        int size = chars.length;

        if (size == 2) {
            moduleInfo.error(stmt.name, "Import name cannot be empty.");
            return null;
        }

        int i = 0;
        for (char c : chars) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') ||
                    c == '\\' || c == '/' || c == '-' || c == '_' || c == '.') {
                if (c == '\\' || c == '/' || c == '-' || c == '.') {
                    needsAs = true;
                }
            } else if (i != 0 && i != size - 1){
                System.out.println(c);
                moduleInfo.error(stmt.name, "Import name must be a valid module name.");
                return null;
            }
            i += 1;
        }

        if (chars[size - 2] == '\\' || chars[size - 2] == '/') {
            moduleInfo.error(stmt.name, "Import name must not end with a slash.");
            return null;
        }

        if (needsAs) {
            if (stmt.alias == null) {
                Token errMessage = new Token(TokenType.SEMICOLON, ";", null, stmt.name.line);
                moduleInfo.error(errMessage, "Expected alias: Import name contains one or more of following characters: '\\', '/', '-', '.'.");
            } else {
                declare(stmt.alias);
                define(stmt.alias);
            }
        } else {
            declare(stmt.name);
            define(stmt.name);
        }

        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);

        if (stmt.superclass != null && stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
            moduleInfo.error(stmt.superclass.name, "A class can't inherit from itself.");
        }

        if (stmt.superclass != null) {
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superclass);
        }

        if (stmt.superclass != null) {
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            assert method.name != null;
            if (method.name.lexeme.equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }

            resolveFunction(method, declaration);
        }

        endScope();

        if (stmt.superclass != null) endScope();

        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            moduleInfo.error(stmt.keyword, "Can't return from top-level code.");
        }

        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                moduleInfo.error(stmt.keyword, "Can't return a value from an initializer.");
            }

            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);

        if (stmt.varType == TokenType.CONST) {
            if (constants.isEmpty()) {
                constants.push(new HashSet<>());
            }
            constants.peek().add(stmt.name.lexeme);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        beginLoop();
        resolve(stmt.condition);
        resolve(stmt.body);
        endLoop();
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (!isInLoop) {
            moduleInfo.error(stmt.keyword, "Can't use 'break' outside of a loop.");
        }
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) {
        if (!isInLoop) {
            moduleInfo.error(stmt.keyword, "Can't use 'continue' outside of a loop.");
        }
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);

        if (!constants.isEmpty() && constants.peek().contains(expr.name.lexeme)) {
            moduleInfo.error(expr.name, "Cannot reassign a constant.");
        }

        resolveLocal(expr, expr.name);

        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitLambdaExpr(Expr.Lambda expr) {
        resolveFunction(expr.function, FunctionType.LAMBDA);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.obj);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);

        if (expr.obj instanceof Expr.Variable variable) {
            if (!constants.isEmpty() && constants.peek().contains(variable.name.lexeme)) {
                moduleInfo.error(expr.name, "Cannot modify a field of a constant object.");
                return null;
            }
        }

        resolve(expr.obj);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if (currentClass == ClassType.NONE) {
            moduleInfo.error(expr.keyword, "Can't use 'super' outside of a class.");
        } else if (currentClass != ClassType.SUBCLASS) {
            moduleInfo.error(expr.keyword, "Can't use 'super' in a class with no superclass.");
        }

        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            moduleInfo.error(expr.keyword, "Can't use 'this' outside of a class.");
            return null;
        }

        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            moduleInfo.error(expr.name, "Can't read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
        constants.push(new HashSet<>());
    }

    private void endScope() {
        scopes.pop();
        constants.pop();
    }

    private void beginLoop() {
        isInLoop = true;
    }

    private void endLoop() {
        isInLoop = false;
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            moduleInfo.error(name, "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }
}
