package lox

import lox.token.Token
import lox.token.TokenType

class ModuleInfo(
    val name: String,
) {
    @JvmField var hadError: Boolean = false
    @JvmField var hadRuntimeError: Boolean = false

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '" + token.lexeme + "'", message)
        }
    }

    private fun report(line: Int, where: String, message: String) {
        if (name == "__main__") {
            System.err.println("[line ${line}] Error${where}: ${message}")
        } else {
            System.err.println("In module '${name}' on [line ${line}] Error${where}: ${message}")
        }
        hadError = true
    }

    fun runtimeError(error: RuntimeError) {
        if (name == "__main__") {
            System.err.println("${error.message}\n[line ${error.token.line}]")
        } else {
            System.err.println("${error.message}\n[line ${error.token.line}] in module '${name}'")
        }
        hadRuntimeError = true
    }
}
