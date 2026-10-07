package org.example.toylang;

import java.util.List;                        // needed because Block holds a list

// A Stmt is something that DOES an action rather than producing a value.
public sealed interface Stmt {
    record Assign(String name, Expr value) implements Stmt {}   // x = 1 + 2;
    record Print(Expr value) implements Stmt {}                 // print x;
    // elseBranch is null when there is no else
    record If(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {}
    record While(Expr condition, Stmt body) implements Stmt {}  // while (cond) body
    record Block(List<Stmt> statements) implements Stmt {}      // { ... } holds any number of statements
}