Toylang

A small programming language written in Java, built from scratch as a learning project: a lexer, a parser and a tree-walking interpreter.

The goal is to understand how a compiler front end works by building every stage by hand, without parser generators.

Status
Lexer (tokens, keywords, operators, line tracking, error reporting) x
AST (expression and statement nodes) x
Parser (recursive descent, operator precedence)
Interpreter
Test suite and example programs
Planned language

The language is deliberately small: integer arithmetic, variables, comparisons, if/else, while and print.

n = 5;
result = 1;
while (n > 0) {
result = result * n;
n = n - 1;
}
print result;

This is the target syntax. At the moment the lexer and the AST node types are implemented, so programs can be tokenised but not yet parsed or run.

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
Example

Input:

while (n > 0) { n = n - 1; }

Output (token type and lexeme):

WHILE(while) LPAREN(() IDENT(n) GT(>) NUMBER(0) RPAREN())
LBRACE({) IDENT(n) ASSIGN(=) IDENT(n) MINUS(-) NUMBER(1)
SEMICOLON(;) RBRACE(}) EOF
Design decisions
Keywords beat identifiers. A word is read as an identifier-shaped lexeme first, then looked up in the keyword table. If it is found, it becomes a keyword token; otherwise it is an IDENT.
Longest match. A word is read for as long as it continues to match, so ifthen is a single identifier rather than the keyword if followed by then.
One character of lookahead. Operators that may be one or two characters (=/==, </<=, >/>=) are decided by peeking at the next character. A helper, match, consumes that character only when it is the expected one, so a failed check never swallows input.
Single pass, left to right. The lexer never backtracks.
The AST

The parser will turn the flat token list into a tree. The tree nodes are defined as Java records inside two sealed interfaces, Expr (things that produce a value) and Stmt (things that perform an action).

For example, 1 + 2 * 3 and (1 + 2) * 3 use the same tokens but give different trees:

        +                    *
       / \                  / \
      1   *                +   3
         / \              / \
        2   3            1   2
Design decisions
No node for brackets. Grouping is recorded by the shape of the tree: whatever sits lower is evaluated first.
Sealed interfaces. Every node kind is known up front, so the interpreter's switch can be checked for completeness.
Operators are stored as tokens, so error messages can report the line they came from.
Project structure
src/main/java/org/example/toylang/
Main.java        entry point
Lexer.java       source text -> list of tokens
Token.java       a token: type, lexeme, line
TokenType.java   all token types
LangError.java   error type carrying a line number
Expr.java        expression nodes
Stmt.java        statement nodes

The parser and interpreter files will be added in the next stages.

Building and running

Requirements: a JDK that supports records and sealed types (Java 17 or newer). The pom.xml currently targets Java 25; change the source and target values if your JDK is older.

From IntelliJ: open the project, then run Main.

From the command line, using the bundled Maven wrapper:

./mvnw compile

(On Windows, use mvnw.cmd compile.)

Roadmap
Parser: recursive descent, with one method per grammar rule and one precedence level per rule.
Interpreter: a tree-walking evaluator with a variable store.
Tests: example programs with expected output, run by a single command.
Possible extensions: functions, a type checker, compiling to bytecode for a small virtual machine, and simple optimisations such as constant folding.