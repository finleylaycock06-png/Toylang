package org.example.toylang;

public sealed interface Expr {
    record Num(double value) implements Expr {}
    record Var(String name) implements Expr {}
    record Binary(Expr left, Token op, Expr right) implements Expr {}
    record Unary(Token op, Expr right) implements Expr {}
}
