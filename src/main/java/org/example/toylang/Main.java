package org.example.toylang;

public class Main {
    public static void main(String[] args) {
        Expr e = new Expr.Binary(
                new Expr.Num(1),
                        new Token(TokenType.PLUS, "+", 1),
                  new Expr.Binary(
                  new Expr.Num(2),
                  new Token(TokenType.STAR, "*", 1),
                        new Expr.Num(3)
                )
        );
        System.out.println(e);
        Expr e2 = new Expr.Binary(
                new Expr.Binary(
                        new Expr.Num(1),
                        new Token(TokenType.PLUS, "+", 1),
                        new Expr.Num(2)
                ),
                new Token(TokenType.STAR, "*", 1),
                new Expr.Num(3)
        );
        System.out.println(e2);
    }

}
