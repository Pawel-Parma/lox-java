package lox

import lox.token.Token
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


class LoxModule(
    private val name: Token,
) {
    private val info = ModuleInfo(name.literal.toString())
    @JvmField val interpreter: Interpreter = Interpreter(info)

    fun init() {
        val bytes: ByteArray?
        val path = name.literal.toString() + ".lox"
        try {
            bytes = Files.readAllBytes(Paths.get(path))
        } catch (e: Exception) {
            System.err.println("Error reading file: ${path}")
            exitProcess(71)
        }

        val scanner = Scanner(String(bytes!!, Charset.defaultCharset()), info)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens, info)
        val statements = parser.parse()

        // Stop if there was a syntax error.
        if (info.hadError) {
            exitProcess(65)
        }

        val resolver = Resolver(interpreter, info)
        resolver.resolve(statements)

        // Stop if there was a resolution error.
        if (info.hadError) {
            exitProcess(65)
        }

        interpreter.interpret(statements)
    }

    fun get(name: Token): Any? {
        return interpreter.environment[name]
    }

    fun set(name: Token, value: Any?) {
        interpreter.environment.assign(name, value)
    }
}
