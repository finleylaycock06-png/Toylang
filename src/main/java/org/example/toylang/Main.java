package org.example.toylang;

public class Main {
    public static void main(String[] args) {
        System.out.println(new Lexer("while (n > 0) { n = n - 1; }").lex());
    }

}
