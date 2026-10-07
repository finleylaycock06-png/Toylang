package org.example.toylang;

// A record is a small class that just holds data.
// Java generates the constructor, the getters (type(), lexeme(), line()) and toString for us.
// type   = which kind of token it is
// lexeme = the exact text it was made from (e.g. "while", "42", "<=")
// line   = the line number in the source, used for error messages
public record Token(TokenType type, String lexeme, int line) {}