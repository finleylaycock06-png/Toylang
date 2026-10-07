package org.example.toylang;

// An enum is a fixed list of named constants.
// Each constant is one KIND of token the lexer can produce.
public enum TokenType {
    NUMBER, IDENT,                      // a number like 42, or a name like x1
    IF, ELSE, WHILE, PRINT,             // keywords (reserved words)
    PLUS, MINUS, STAR, SLASH,           // arithmetic: + - * /
    ASSIGN, EQ, LT, LE, GT, GE,         // = == < <= > >=
    LPAREN, RPAREN,                     // round brackets ( )
    LBRACE, RBRACE,                     // curly brackets { }
    SEMICOLON,                          // ;
    EOF                                 // "end of file": the last token, marks the end of input
}