
Toylang
A small programming language written in Java, built from scratch as a learning project: a lexer, a parser and a tree-walking interpreter.

The goal is to understand how a language implementation works by building every stage by hand, without parser generators.

Status
Lexer (tokens, keywords, operators, line tracking, error reporting)
AST (expression and statement nodes)
Parser (recursive descent, operator precedence, statements)
Interpreter (tree-walking evaluator with a variable store)
Runs programs from a file given on the command line, with line-numbered error messages
Example programs and an automated test suite (JUnit 5)
Unary minus
The language
Arithmetic, variables, comparisons, if/else, while, blocks and print. Every statement ends with ;. All numbers are doubles.

n = 5;
result = 1;
while (n > 0) {
result = result * n;
n = n - 1;
}
print result;
Output:

120.0

How it works
source text  --Lexer-->  List<Token>  --Parser-->  AST (List<Stmt>)  --Interpreter-->  output
The lexer turns text into tokens.
The parser turns tokens into a tree.
The interpreter walks the tree and runs it.
The lexer
The lexer reads the source text left to right in a single pass and produces a list of tokens. Whitespace is discarded (newlines are counted so errors can report a line number), and every token records the line it appeared on.

Token types
Category	Tokens
Literals and names	NUMBER, IDENT
Keywords	IF, ELSE, WHILE, PRINT
Arithmetic	PLUS, MINUS, STAR, SLASH
Assignment and comparison	ASSIGN (=), EQ (==), LT (<), LE (<=), GT (>), GE (>=)
Punctuation	LPAREN, RPAREN, LBRACE, RBRACE, SEMICOLON
End of input	EOF

Design decisions
Keywords beat identifiers. A word is read as an identifier-shaped lexeme first, then looked up in the keyword table.
Longest match. ifthen is a single identifier, not if followed by then.
One character of lookahead. =/==, </<= and >/>= are decided by peeking at the next character. The match helper consumes it only when it is the expected one.
Single pass, left to right. The lexer never backtracks.
Unexpected characters (for example @) raise a LangError with the line number.
The AST
The tree nodes are Java records inside two sealed interfaces: Expr (things that produce a value) and Stmt (things that perform an action).

Interface	Nodes
Expr	Num, Var, Binary, Unary
Stmt	Assign, Print, If, While, Block
1 + 2 * 3 and (1 + 2) * 3 use the same tokens but give different trees:

        +                    *
       / \                  / \
      1   *                +   3
         / \              / \
        2   3            1   2

Design decisions
No node for brackets. Grouping is recorded by the shape of the tree.
Sealed interfaces. Every node kind is known up front, so the compiler checks that the interpreter's switch covers all of them.
Operators are stored as tokens, so error messages can report the line they came from.
"No else" is null. An If node's elseBranch is null when the else is absent.

The parser
A hand-written recursive descent parser: one method per grammar rule.

Grammar
program     → statement* EOF
statement   → printStmt | block | whileStmt | ifStmt | assignment
printStmt   → "print" expression ";"
block       → "{" statement* "}"
whileStmt   → "while" "(" expression ")" statement
ifStmt      → "if" "(" expression ")" statement ( "else" statement )?
assignment  → IDENT "=" expression ";"

expression  → comparison
comparison  → term ( ( "<" | "<=" | ">" | ">=" | "==" ) term )*
term        → factor ( ( "+" | "-" ) factor )*
factor      → unary ( ( "*" | "/" ) unary )*
unary       → primary
primary     → NUMBER | IDENT | "(" expression ")"
unary is currently a pass-through, so negative literals such as -5 are not supported yet. Write 0 - 5 instead.

Operator precedence
Each precedence level is its own rule, and each rule calls the next-tighter one. From loosest to tightest:

Level	Operators
comparison	< <= > >= ==
term	+ -
factor	* /
primary	numbers, variables, brackets

Design decisions
Loops give left-to-right grouping. 10 - 3 - 2 parses as (10 - 3) - 2.
Recursion handles nesting. primary calls expression for brackets, and block calls statement.
else attaches to the nearest if, as in C and Java.
Errors carry a line number. consume throws a LangError naming the expected token and the line.

The interpreter
A tree-walking interpreter: it runs a program by walking the AST directly, with no bytecode or compilation step. It has two methods and a variable store.

Part	Role
evaluate(Expr)	Recursive. Works out an expression tree and returns a double.
execute(Stmt)	Runs one statement. Returns nothing; it prints or changes the variables.
variables	A Map<String, Double> holding the current value of every variable.

Design decisions
One number type. Every value is a double, so there is no type checking to do.
Booleans are numbers. Comparisons give 1.0 (true) or 0.0 (false), and if/while treat any non-zero value as true.
while re-evaluates its condition every pass, so changes made in the body are seen.
Pattern-matching switch over sealed types. Adding a new node type without handling it in the interpreter is a compile error.
Runtime errors (division by zero, undefined variable) raise a LangError, reported as Error on line N: message.
Known limitations
Undefined-variable errors report line 0, because Var nodes do not store a line number yet.
Output is always printed as a double (120.0, not 120).

Project structure
programs/
factorial.toy    prints 120.0
countdown.toy    prints 3.0, 2.0, 1.0
sum.toy          prints 55.0
ifelse.toy       prints 1.0
error.toy        reports a division by zero
src/main/java/org/example/toylang/
Main.java        entry point: reads the file given as an argument, runs it, reports errors
Lexer.java       source text -> list of tokens
Token.java       a token: type, lexeme, line
TokenType.java   all token types
LangError.java   error type carrying a line number
Expr.java        expression nodes
Stmt.java        statement nodes
Parser.java      list of tokens -> list of statements (the AST)
Interpreter.java walks the AST and runs the program
src/test/java/org/example/toylang/
ProgramsTest.java  runs every example program and checks its output

Building and running
Requirements: a JDK that supports records, sealed types and pattern-matching switch (Java 21 or newer). The pom.xml currently targets Java 25; change the source and target values if your JDK is older.
Run a program by passing its path as the argument:
java -cp target/classes org.example.toylang.Main programs/factorial.toy
(Build first with ./mvnw compile; on Windows use mvnw.cmd compile.)
From IntelliJ: open the project, then in Run -> Edit Configurations put the program path (for example programs/sum.toy) in Program arguments and run Main.

Testing
Every example program has a test in ProgramsTest. Each test runs a program through Main, captures what it prints, and compares it with the expected output.
./mvnw test

Program	Expected output	Covers
factorial.toy	120.0	while, *, -, >
countdown.toy	3.0, 2.0, 1.0	a loop printing several lines
sum.toy	55.0	<=, +, running totals
ifelse.toy	1.0	if/else
error.toy	Error on line 1: Division by zero	runtime errors with line numbers

Errors
A broken program reports the line it failed on:
print 1 / 0;
Error on line 1: Division by zero

Roadmap
Unary minus: finish unary() so -5 works.
Line numbers on variables for better undefined-variable errors.
Whole-number output: print 120 instead of 120.0.
More tests: unit tests for the lexer and parser, and for error cases.
Possible extensions: functions, a type checker, compiling to bytecode for a small virtual machine, and simple optimisations such as constant folding.


