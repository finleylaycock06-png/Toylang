package org.example.toylang;

public record Token (TokenType type, String lexeme, int line){}
