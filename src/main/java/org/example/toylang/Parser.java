package org.example.toylang;

import java.util.List;
import java.util.ArrayList;                   // lets us create growable lists
public class Parser {
    private final List<Token> tokens;         // the token list from the lexer
    private int current = 0;                  // index of the next token to look at

    public Parser(List<Token> tokens) {
        this.tokens = tokens;                 // store the input
    }

    public List<Stmt> parse(){                // public so can be called by main; returns every statement in the program
     List<Stmt> program = new ArrayList<>();  // start with an empty list to collect statements
     while (!isAtEnd()){                      // keep going until we reach the EOF token
         program.add(statement());            // parse one statement and add it to the list
     }
     return program;                          // after the loop: hand back the finished list
    }

    // ===== STATEMENTS =====
    private Stmt statement(){
        if (match(TokenType.PRINT)){          // is the nest token "print?" match() also consume it
            return printStatement();          // yes, so the print keyword is gone: parse the rest
        }
        if (match(TokenType.LBRACE)){         // is the nest toke "{?" it is consumed too
            return block();                   // parse everything up to the matching "}"
        }
        if (match(TokenType.WHILE)){          // NEW: next token is "while"? it is consumed
            return whileStatement();          // parse the rest of the while loop
        }
        if (match(TokenType.IF)){             // NEW: next token is "if"? it is consumed
            return ifStatement();             // parse the rest of the if statement
        }
        return assignment();                  // default: anything else must start with a variable name
    }
    private Stmt block(){
        List<Stmt> statements = new ArrayList<Stmt>();   // a list to collect the statements inside the braces
        // keep going while the next token is not "}" and we have not run out of tokens
        while (!check(TokenType.RBRACE) && !isAtEnd()){
            statements.add(statement());      // parse one statement (may itself be a block: recursion)
        }
        consume(TokenType.RBRACE,             // after the loop the closing "}" is required, once
                "Expected '}' after block");
        return new Stmt.Block(statements);    // wrap the list in a Block node and return it
    }
    private Stmt whileStatement(){
        consume(TokenType.LPAREN,             // the "(" must come right after the "while"
                "Expected '(' after 'while'");    // error message if it is missing
        Expr condition = expression();            // parse the condition, e.g n > 0
        consume(TokenType.RPAREN,                 // the ")" closes the condition
                "Expected ')' after 'condition'");
        Stmt body = statement();                  // parse the body: one statement, usually a block (recursion)
        return new Stmt.While(condition, body);   // build a while node (condition first, then body) and return it
    }
    private Stmt ifStatement(){
consume(TokenType.LPAREN,                    // the "(" must come right after the "if" (the keyword is already consumed)
        "Expected '(' after 'if'");
Expr condition = expression();               // parse the condition, e.g x < 1
consume(TokenType.RPAREN,                    // the ")" closes the condition
        "Expected ')' after condition");
Stmt thenBranch = statement();               // parse what runs when the condition is true
Stmt elseBranch = null;                      // null means "no else branch" (default)
if (match(TokenType.ELSE)){                  // is there an "else"? if so it is consumed
    elseBranch = statement();                // parse what runs when the condition is false
        }
// outside the if block, so it runs whether or not there was an else
return new Stmt.If(condition, thenBranch, elseBranch);   // build an If node and return it
    }
 private Stmt printStatement(){
Expr e = expression();                       // parse what is being printed (the "print" word is already consumed)
consume(TokenType.SEMICOLON,                 // the statement must end with ";"
        "Expected ';' after value");         // error message if the ";" is missing
return new Stmt.Print(e);                    // build a Print node holding the expression, and return it
    }
  private   Stmt assignment(){
Token nameOfToken = consume(TokenType.IDENT,  // the next token must be a variable name; consume returns it
        "Expected variable name");
String name = nameOfToken.lexeme();           // get the name as plain text, e.g "x"
consume(TokenType.ASSIGN,                     // the next token must be "="
        "Expected '=' after variable name");
Expr expres = expression();                   // parse the value on the right hand side
consume(TokenType.SEMICOLON,                  // teh statement must end with ";"
        "Expected ';' after value");
return new Stmt.Assign(name,expres);          // build an Assign node (name first, then value) and return it
    }


    // ===== GRAMMAR METHODS =====
    // Each level calls the next-tighter one, so * binds tighter than +, and + tighter than <.
    // Chain: expression -> comparison -> term -> factor -> unary -> primary

    Expr expression() {
        return comparison();                  // an expression starts at the loosest-binding level
    }

    // comparison -> term ( (< | <= | > | >= | ==) term )*
    private Expr comparison() {
        Expr left = term();                   // parse the left side (the next-tighter level)
        while (match(TokenType.LT, TokenType.LE, TokenType.GT, TokenType.GE, TokenType.EQ)) {
            Token op = previous();            // the comparison operator we just consumed
            Expr right = term();              // parse the right side
            left = new Expr.Binary(left, op, right);   // wrap everything so far as the new left
        }
        return left;                          // no more operators, so we are done
    }

    // term -> factor ( (+ | -) factor )*
    private Expr term() {
        Expr left = factor();                 // parse the left side
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();            // + or -
            Expr right = factor();            // parse the right side
            left = new Expr.Binary(left, op, right);   // loop gives left-to-right: 10-3-2 = (10-3)-2
        }
        return left;
    }

    // factor -> unary ( (* | /) unary )*
    private Expr factor() {
        Expr left = unary();
        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token op = previous();            // * or /
            Expr right = unary();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    // unary -> "-" unary | primary   (temporary stub: just passes through)
    private Expr unary() {
        return primary();
    }

    // primary -> NUMBER | IDENT | "(" expression ")"   (the base case)
    private Expr primary() {
        if (match(TokenType.NUMBER)) {        // a number token
            // previous() is the number token; turn its text into a double and wrap it in a node
            return new Expr.Num(Double.parseDouble(previous().lexeme()));
        }
        if (match(TokenType.IDENT)) {         // a variable name
            return new Expr.Var(previous().lexeme());
        }
        if (match(TokenType.LPAREN)) {        // an opening bracket
            Expr e = expression();            // parse everything inside (recursion)
            consume(TokenType.RPAREN, "Expected ')' after expression");   // the ) is required
            return e;                         // no node for the brackets: the tree shape holds the grouping
        }
        // none of the cases matched, so the input is not a valid expression
        throw new LangError("Expected expression", peek().line());
    }

    // ===== HELPERS (the parser's version of peek/advance/match) =====

    private Token peek() {
        return tokens.get(current);           // the current token, without consuming it
    }

    private Token previous() {
        return tokens.get(current - 1);       // the token we just consumed
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;   // EOF marks the end of the token list
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {                      // never match a real type at EOF
            return false;
        }
        return peek().type() == type;         // is the current token of this type?
    }

    private Token advance() {
        if (!isAtEnd()) {                     // do not walk past EOF
            current += 1;                     // move to the next token
        }
        return previous();                    // return the token we just passed
    }

    // TokenType... is "varargs": it accepts any number of types, available inside as an array.
    private boolean match(TokenType... types) {
        for (TokenType type : types) {        // try each type in turn
            if (check(type)) {                // does the current token have this type?
                advance();                    // yes, so consume it
                return true;
            }
        }
        return false;                         // none matched, and nothing was consumed
    }

    // Like match, but the token is REQUIRED: if it is missing, that is a syntax error.
    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();                 // found it, so consume and return it
        }
        throw new LangError(message, peek().line());   // missing, so report the line of the bad token
    }
}