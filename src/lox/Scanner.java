package lox;

import lox.token.Token;
import lox.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.token.TokenType.*;

class Scanner {
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",       AND);
        keywords.put("class",     CLASS);
        keywords.put("else",      ELSE);
        keywords.put("false",     FALSE);
        keywords.put("for",       FOR);
        keywords.put("def",       DEF);
        keywords.put("fun",       FUN);
        keywords.put("lambda",    LAMBDA);
        keywords.put("import",    IMPORT);
        keywords.put("as",        AS);
        keywords.put("if",        IF);
        keywords.put("nil",       NIL);
        keywords.put("or",        OR);
        keywords.put("print",     PRINT);
        keywords.put("return",    RETURN);
        keywords.put("super",     SUPER);
        keywords.put("this",      THIS);
        keywords.put("true",      TRUE);
        keywords.put("var",       VAR);
        keywords.put("const",     CONST);
        keywords.put("while",     WHILE);
        keywords.put("break",     BREAK);
        keywords.put("continue",  CONTINUE);
    }

    private final String source;
    private final ModuleInfo moduleInfo;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source, ModuleInfo moduleInfo) {
        this.source = source;
        this.moduleInfo = moduleInfo;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '%': addToken(PERCENT); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                line++;
                break;

            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    moduleInfo.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "." then the digits.
            do advance();
            while (isDigit(peek()));
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        StringBuilder sb = new StringBuilder();
        boolean currentCharSlash = false;
        char preprepreviousChar = '\0';
        char prepreviousChar = '\0';
        char previousChar = '\0';
        char currentChar = '\0';
        while ((peek() != '"' || currentChar == '\\' && prepreviousChar != '\\') && !isAtEnd()) {
            if (peek() == '\n') line++;
            currentChar = source.charAt(current);

            if ((previousChar == '\\' && prepreviousChar != '\\') || (previousChar == '\\' && preprepreviousChar == '\\')) {
                switch (currentChar) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case '\'':
                        sb.append("'");
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    default:
                        moduleInfo.error(line, "Invalid escape sequence: '" + previousChar + currentChar + "'.");
                        break;
                }
            } else if (currentChar == '\\') {
                currentCharSlash = true;
            } else if (currentCharSlash) {
                sb.append(currentChar);
                currentCharSlash = false;
            } else {
                sb.append(currentChar);
            }

            current++;
            preprepreviousChar = prepreviousChar;
            prepreviousChar = previousChar;
            previousChar = currentChar;
        }

        if (isAtEnd()) {
            moduleInfo.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        String value = sb.toString();
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
