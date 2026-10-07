package org.example.toylang;

import java.util.ArrayList;                   // a growable list implementation
import java.util.HashMap;                     // a lookup table implementation
import java.util.List;                        // the list interface
import java.util.Map;                         // the lookup table interface

import static org.example.toylang.TokenType.*;   // lets us write PLUS instead of TokenType.PLUS

public class Lexer {

    // Lookup table from a word to its keyword token type.
    // static = one shared copy for the whole class; final = never reassigned.
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {                                  // runs once, when the class is first loaded
        KEYWORDS.put("if", IF);               // the word "if" is the IF keyword
        KEYWORDS.put("else", ELSE);
        KEYWORDS.put("while", WHILE);
        KEYWORDS.put("print", PRINT);
    }

    private final String src;                 // the whole program text; final = set once in the constructor
    private int pos = 0;                      // index of the next character to read
    private int line = 1;                     // current line number, starts at 1

    public Lexer(String src) {
        this.src = src;                       // store the input (this.src is the field, src is the parameter)
    }

    // The main method: turns the whole text into a list of tokens.
    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();    // start with an empty list
        while (true) {                        // repeat until we break out
            skipWhitespace();                 // move past spaces, tabs and newlines
            if (atEnd()) break;               // nothing left to read, so stop
            tokens.add(nextToken());          // read one token and add it to the list
        }
        tokens.add(new Token(EOF, "", line)); // always finish with an EOF token
        return tokens;                        // hand back the finished list
    }

    // Moves pos past whitespace and counts newlines.
    private void skipWhitespace() {
        // loop while there is a character AND it is a space, tab, carriage return or newline
        while (!atEnd() && (peek() == ' ' || peek() == '\t'
                || peek() == '\r' || peek() == '\n')) {
            if (peek() == '\n') line++;       // a newline means we are on the next line
            advance();                        // consume the whitespace character
        }
    }

    // Reads exactly one token starting at pos.
    private Token nextToken() {
        char c = advance();                   // read the next character and move forward

        if (Character.isDigit(c)) {           // starts with a digit, so it is a number
            return lexNumber();
        }
        if (Character.isLetter(c)) {          // starts with a letter: keyword or identifier
            return lexWord();
        }

        switch (c) {                          // otherwise decide by the character itself
            case '+': return new Token(PLUS, "+", line);
            case '-': return new Token(MINUS, "-", line);
            case '*': return new Token(STAR, "*", line);
            case '/': return new Token(SLASH, "/", line);
            case '(': return new Token(LPAREN, "(", line);     // paren = round bracket
            case ')': return new Token(RPAREN, ")", line);
            case '{': return new Token(LBRACE, "{", line);     // brace = curly bracket
            case '}': return new Token(RBRACE, "}", line);
            case ';': return new Token(SEMICOLON, ";", line);

            case '=':
                if (match('=')) return new Token(EQ, "==", line);   // "==" if a second = follows
                return new Token(ASSIGN, "=", line);                // otherwise a single =
            case '<':
                if (match('=')) return new Token(LE, "<=", line);   // "<=" if = follows
                return new Token(LT, "<", line);                    // otherwise just <
            case '>':
                if (match('=')) return new Token(GE, ">=", line);   // ">=" if = follows
                return new Token(GT, ">", line);                    // otherwise just >

            default:                          // any other character is not allowed
                throw new LangError("Unexpected character '" + c + "'", line);
        }
    }

    // Reads the rest of a number. The first digit was already consumed by nextToken().
    private Token lexNumber() {
        int start = pos - 1;                  // the number began one character before pos
        while (!atEnd() && Character.isDigit(peek())) {   // keep going while the next char is a digit
            advance();                        // consume it
        }
        String text = src.substring(start, pos);          // the text from start up to (not including) pos
        return new Token(NUMBER, text, line);
    }

    // Reads the rest of a word. The first letter was already consumed.
    private Token lexWord() {
        int start = pos - 1;                  // the word began one character before pos
        // keep going while the next char is a letter, digit or underscore
        while (!atEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            advance();
        }
        String text = src.substring(start, pos);          // the whole word
        TokenType type = KEYWORDS.get(text);              // look it up; null if it is not a keyword
        if (type == null) {                   // not in the table, so it is a plain name
            return new Token(IDENT, text, line);
        }
        return new Token(type, text, line);   // it is a keyword (keywords beat identifiers)
    }

    // Consume the next character ONLY if it equals expected. Returns whether it did.
    private boolean match(char expected) {
        if (atEnd()) {                        // nothing left to compare, so no match
            return false;
        }
        if (peek() != expected) {             // a different character, so leave it for the next token
            return false;
        }
        pos += 1;                             // it matches, so consume it
        return true;
    }

    private boolean atEnd() {
        return pos >= src.length();           // true once pos has run past the last character
    }

    private char peek() {
        return src.charAt(pos);               // look at the current character without moving
    }

    private char advance() {
        return src.charAt(pos++);             // return the current character, THEN move pos forward
    }
}