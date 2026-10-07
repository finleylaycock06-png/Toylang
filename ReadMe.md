
Toylang
A small programming language written in Java, built from scratch as a learning project: a lexer, a parser and a tree-walking interpreter.

The goal is to understand how a compiler front end works by building every stage by hand, without parser generators.

Status
Lexer (tokens, keywords, operators, line tracking, error reporting)   x
AST (expression and statement nodes)    x
Parser (recursive descent, operator precedence, statements)    x
Interpreter
Test suite and example programs


Planned language
The language is deliberately small: arithmetic, variables, comparisons, if/else, while, blocks and print.

n = 5;
result = 1;
while (n > 0) {
result = result * n;
n = n - 1;
}
print result;
At the moment programs can be tokenised and parsed into a tree, but not yet run. The interpreter is the next stage.

How it works
source text  --Lexer-->  List<Token>  --Parser-->  AST (List<Stmt>)  --Interpreter-->  output
The lexer turns text into tokens.
The parser turns tokens into a tree.
The interpreter (not yet written) walks the tree and runs it.
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
Lexical rules
Numbers: a non-empty run of digits.
Identifiers: start with a letter, followed by any letters, digits or _.
Keywords: looked up in a table after an identifier-shaped word has been read.
Unexpected characters (for example @) raise a LangError with the line number.
Design decisions
Keywords beat identifiers. A word is read as an identifier-shaped lexeme first, then looked up in the keyword table. If it is found, it becomes a keyword token; otherwise it is an IDENT.
Longest match. A word is read for as long as it continues to match, so ifthen is a single identifier rather than the keyword if followed by then.
One character of lookahead. Operators that may be one or two characters (=/==, </<=, >/>=) are decided by peeking at the next character. A helper, match, consumes that character only when it is the expected one, so a failed check never swallows input.
Single pass, left to right. The lexer never backtracks.
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
No node for brackets. Grouping is recorded by the shape of the tree: whatever sits lower is evaluated first.
Sealed interfaces. Every node kind is known up front, so the interpreter's switch can be checked for completeness.
Operators are stored as tokens, so error messages can report the line they came from.
"No else" is null. An If node's elseBranch is null when the else is absent.
The parser
A hand-written recursive descent parser: one method per grammar rule. It reads the token list and builds the AST, and it reports syntax errors with a line number.

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
Each precedence level is its own rule, and each rule calls the next-tighter one. That is what makes * bind tighter than +, and + tighter than <. From loosest to tightest:

Level	Operators
comparison	< <= > >= ==
term	+ -
factor	* /
primary	numbers, variables, brackets
Design decisions
Loops give left-to-right grouping. Each binary level uses a while loop that wraps everything so far as the new left child, so 10 - 3 - 2 parses as (10 - 3) - 2.
Recursion handles nesting. primary calls expression for bracketed input, and block calls statement, so brackets and blocks nest to any depth.
else attaches to the nearest if. The inner call to ifStatement() sees the else first, as in C and Java.
Helper methods (peek, advance, match, check, consume) mirror the lexer's character helpers, but work on tokens.
Errors carry a line number. consume throws a LangError naming the expected token and the line.
Example
Input:

if (x < 1) print x; else print 0;
Tree (simplified):

If
├─ condition: Binary( Var x, <, Num 1 )
├─ then:      Print( Var x )
└─ else:      Print( Num 0 )
Project structure
src/main/java/org/example/toylang/
Main.java        entry point
Lexer.java       source text -> list of tokens
Token.java       a token: type, lexeme, line
TokenType.java   all token types
LangError.java   error type carrying a line number
Expr.java        expression nodes
Stmt.java        statement nodes
Parser.java      list of tokens -> list of statements (the AST)
The interpreter file will be added in the next stage.

Building and running
Requirements: a JDK that supports records and sealed types (Java 17 or newer). The pom.xml currently targets Java 25; change the source and target values if your JDK is older.

From IntelliJ: open the project, then run Main.

From the command line, using the bundled Maven wrapper:

./mvnw compile
(On Windows, use mvnw.cmd compile.)

Roadmap
Interpreter: a tree-walking evaluator with a variable store (Map<String, Double>).
Command-line entry point: read a program from a file passed as an argument.
Tests: example programs with expected output, run by a single command.
Unary minus: finish unary() so -5 works.
Possible extensions: functions, a type checker, compiling to bytecode for a small virtual machine, and simple optimisations such as constant folding.

