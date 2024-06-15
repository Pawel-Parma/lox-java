package lox.tool_gen

import lox.token.Token
import lox.token.TokenType


abstract class Stmt {
    interface Visitor<R> {
        fun visitImportStmt(stmt: Import): R
        fun visitBlockStmt(stmt: Block): R
        fun visitClassStmt(stmt: Class): R
        fun visitExpressionStmt(stmt: Expression): R
        fun visitFunctionStmt(stmt: Function): R
        fun visitIfStmt(stmt: If): R
        fun visitPrintStmt(stmt: Print): R
        fun visitReturnStmt(stmt: Return): R
        fun visitVarStmt(stmt: Var): R
        fun visitWhileStmt(stmt: While): R
        fun visitBreakStmt(stmt: Break): R
        fun visitContinueStmt(stmt: Continue): R
    }

    class Import(@JvmField val name: Token, @JvmField val alias: Token?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitImportStmt(this)
        }
    }

    class Block(@JvmField val statements: List<Stmt>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBlockStmt(this)
        }
    }

    class Class(@JvmField val name: Token, @JvmField val superclass: Expr.Variable?, @JvmField val methods: List<Function>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitClassStmt(this)
        }
    }

    class Expression(@JvmField val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitExpressionStmt(this)
        }
    }

    class Function(@JvmField val name: Token?, @JvmField val params: List<Token>, @JvmField val body: List<Stmt>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitFunctionStmt(this)
        }
    }

    class If(@JvmField val condition: Expr, @JvmField val thenBranch: Stmt, @JvmField val elseBranch: Stmt?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitIfStmt(this)
        }
    }

    class Print(@JvmField val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitPrintStmt(this)
        }
    }

    class Return(@JvmField val keyword: Token, @JvmField val value: Expr?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitReturnStmt(this)
        }
    }

    class Var(@JvmField val name: Token, @JvmField val initializer: Expr?, @JvmField val varType: TokenType) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVarStmt(this)
        }
    }

    class While(@JvmField val condition: Expr, @JvmField val body: Stmt) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitWhileStmt(this)
        }
    }

    class Break(@JvmField val keyword: Token) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBreakStmt(this)
        }
    }

    class Continue(@JvmField val keyword: Token) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitContinueStmt(this)
        }
    }

    abstract fun <R> accept(visitor: Visitor<R>): R
}
