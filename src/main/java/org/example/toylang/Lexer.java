package org.example.toylang;

import static org.example.toylang.TokenType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class Lexer {

    private final String src;
    private int pos = 0;
    private int line = 1;
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("if", IF);
        KEYWORDS.put("else", ELSE);
        KEYWORDS.put("while", WHILE);
        KEYWORDS.put("print", PRINT);
    }

    public Lexer(String src) {
        this.src = src;
    }


    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            skipWhitespace();
            if (atEnd()) break;
            tokens.add(nextToken());
        }
        tokens.add(new Token(EOF, "", line));
        return tokens;
    }

    private void skipWhitespace() {
        while (!atEnd() && (peek() == ' ' || peek() == '\t'
                || peek() == '\r' || peek() == '\n')) {
            if (peek() == '\n') line++;
            advance();
        }
    }

    private Token nextToken() {
        char c = advance();
        if (Character.isDigit(c)) {
            return lexNumber();
        }
        if (Character.isLetter(c)) {
            return lexWord();
        }
        switch (c) {
            case '+':
                return new Token(PLUS, "+", line);
            case '-':
                return new Token(MINUS, "-", line);
            case '*':
                return new Token(STAR, "*", line);
            case '/':
                return new Token(SLASH, "/", line);
            case '(':
                return new Token(LPAREN, "(", line);
            case ')':
                return new Token(RPAREN, ")", line);
            case '{':
                return new Token(LBRACE, "{", line);
            case '}':
                return new Token(RBRACE, "}", line);
            case ';':
                return new Token(SEMICOLON, ";", line);
            case '=':
                if (match('=')) return new Token(EQ, "==", line);
                return new Token(ASSIGN, "=", line);
            case '<':
                if (match('=')) return new Token(LE, "<=", line);
                return new Token(LT, "<", line);
            case '>':
                if (match('=')) return new Token(GE, ">=", line);
                return new Token(GT, ">", line);
            default:
                throw new LangError("Unexpected character " + c + "'", line);
        }

    }

    private Token lexNumber() {
        int start = pos - 1;
        while (!atEnd() && Character.isDigit(peek())) {
            advance();
        }
        String text = src.substring(start, pos);
        return new Token(NUMBER, text, line);

    }

    private Token lexWord() {
        int start = pos - 1;
        while (!atEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            advance();
        }
        String text = src.substring(start, pos);
        TokenType type = KEYWORDS.get(text);
        if (type == null) {
            return new Token(IDENT, text, line);
        }
        return new Token(type, text, line);
    }

    private boolean atEnd() {
        return pos >= src.length();
    }

    private boolean match(char expected) {
        if (atEnd()) {
            return false;
        }
        if (peek() != expected) {
            return false;
        }
        pos += 1;
        return true;
    }

    private char peek() {
        return src.charAt(pos);
    }

    private char advance() {
        return src.charAt(pos++);
    }
}
