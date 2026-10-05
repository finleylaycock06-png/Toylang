package org.example.toylang;
import java.util.List;

public sealed interface Stmt {
    record Assign(String name, Expr value) implements Stmt {}
    record Print(Expr value) implements Stmt {}
    record If(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {}
    record While(Expr condition, Stmt body) implements Stmt {}
    record Block(List<Stmt> statements) implements Stmt {}
}
