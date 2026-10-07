package org.example.toylang;

// An Expr is anything that produces a value. "sealed" means the list of kinds below is fixed.
public sealed interface Expr {
    record Num(double value) implements Expr {}          // a number literal, e.g. 3
    record Var(String name) implements Expr {}           // a variable, e.g. x
    // Two sub-expressions joined by an operator, e.g. 1 + 2. op is a Token so we know which operator.
    record Binary(Expr left, Token op, Expr right) implements Expr {}
    // One operator and one operand, e.g. -5 (not used by the parser yet)
    record Unary(Token op, Expr right) implements Expr {}
}