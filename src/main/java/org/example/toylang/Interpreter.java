package org.example.toylang;    // same package as every other class

import java.util.List;          // the list of statements the parser gives us
import java.util.Map;           // the map type we declare
import java.util.HashMap;       // the map implementation we create

public class Interpreter {
// the variable store: name -> current value (all numbers are doubles)
// private: only this class touches it: the map object itself never changes (its contents do)
    private final Map<String, Double> variables = new HashMap<>();
// run the whole program: called once from main
    public void run(List<Stmt> program) {
        for (Stmt s : program) {  // for each: take each statement in order
            execute(s);           // run it
        }
    }
// evaluate: take an expression tree and return the number it works out to
    double evaluate(Expr expr) {
        return switch (expr) {    // switch on the node type (sealed, so all cases must be covered)
            case Expr.Num n -> n.value();   // a number literal is just its own value
            case Expr.Var v -> {            // a variable: look it up
                if (!variables.containsKey(v.name())) {   // never assigned?
                    throw new LangError("Undefined variable '" + v.name() + "'", 0);    //line 0: Var nodes have no line yet
                }
                yield variables.get(v.name());    // yield hands the value back out of a block arm
            }
            case Expr.Binary b -> {               // two operands and an operator
                double left = evaluate(b.left()); // recursion: work out the left subtree first
                double right = evaluate(b.right()); // then the right subtree
                yield switch (b.op().type()) {    // choose what to do based on the operator token
                    case PLUS -> left + right;
                    case STAR -> left * right;
                    case MINUS -> left - right;
                    case SLASH -> {               // block arm: needs an if before the value
                        if (right == 0) {         // == compares
                            throw new LangError("Division by zero", b.op().line());   // report the operators line
                        }
                        yield left / right;
                    }
                    // comparisons give 1.0 for true and 0.0 for false (no boolean type)
                    case LT -> left < right ? 1.0 : 0.0;
                    case LE -> left <= right ? 1.0 : 0.0;
                    case GT -> left > right ? 1.0 : 0.0;
                    case GE -> left >= right ? 1.0 : 0.0;
                    case EQ -> left == right ? 1.0 : 0.0;
                    default -> throw new LangError("Unknown operator", b.op().line());   // any other token type
                };
            }
            case Expr.Unary u -> throw new LangError("Unary not written yet", 0);   // placeholder until unary() exists
        };
    }
// execute: run one statement; it foes something rather than returning a value, so it is void
    private void execute(Stmt stmt) {
        switch (stmt) {
            case Stmt.Assign a ->    // name = expression
                    variables.put(a.name(), evaluate(a.value()));   // work out the value, then store it under the name
            case Stmt.Print p ->     // print expression
                    System.out.println(evaluate(p.value()));        // work out the value, then print it
            case Stmt.Block b -> {   // { statements }
                for (Stmt s : b.statements()) {                     // s, not b: b is already the block
                    execute(s);                                     // run each one in order (nested blocks recurse)
                }
            }
            case Stmt.If i -> {
                if (evaluate(i.condition()) != 0.0) {      // non-zero means true
                    execute(i.thenBranch());
                } else if (i.elseBranch() != null) {       // null means "no-else", so check first
                    execute(i.elseBranch());
                }
            }
            case Stmt.While w -> {
                while (evaluate(w.condition()) != 0.0) {   // condition is re-evaluated before every pass
                    execute(w.body());                     // body is usually a block
                }
            }
        }


    }
}
