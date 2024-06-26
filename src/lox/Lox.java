package lox;

import lox.token.Token;
import lox.tool_gen.Stmt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final ModuleInfo moduleInfo = new ModuleInfo("__main__");
    private static final Interpreter interpreter = new Interpreter(moduleInfo);

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: lox-java [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            System.err.println("Error reading file: " + path);
            System.exit(71);
        }
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code.
        if (moduleInfo.hadError) System.exit(65);
        if (moduleInfo.hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            moduleInfo.hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source, moduleInfo);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens, moduleInfo);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (moduleInfo.hadError) return;

        Resolver resolver = new Resolver(interpreter, moduleInfo);
        resolver.resolve(statements);

        // Stop if there was a resolution error.
        if (moduleInfo.hadError) return;

        interpreter.interpret(statements);
    }
}
