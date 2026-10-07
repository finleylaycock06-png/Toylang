package org.example.toylang;

public class Main {
    public static void main(String[] args) {           // the program starts here
        // lex the text into tokens, parse the tokens into a tree, then print the tree
        System.out.println(new Parser(new Lexer("if (x < 1) print x; else print 0;").lex()).parse());
        System.out.println(new Parser(new Lexer("if (x < 1) print x;").lex()).parse());
    }
}