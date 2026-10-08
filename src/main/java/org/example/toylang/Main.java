package org.example.toylang;
import java.io.IOException;    // the exception reading a file can throw
import java.nio.file.Files;    // Files.readString reads a whole file into  a String
import java.nio.file.Path;     // Path.of builds a file path

public class Main {
    // throws an IOException: reading a file can fail (missing file), and Java makes us declare it
    public static void main(String[] args) throws IOException {// path is relative to the project root
       if (args.length == 0) {   // nothing was passed in
           System.err.println("Usage: toylang <file>");
           return;               // stop here
       }

        // lex the text into tokens, parse the tokens into a tree, then print the tree
        try {
             String source = Files.readString(Path.of(args[0]));
             // the whole pipeline in one line, working inside out:
            // Lexer(source).lex()   text -> List<Token>
            // Parser(...).parse()   tokens -> List<Stmt>
            // Interpreter().run(...)  tree -> runs it and prints output
            new Interpreter().run(new Parser(new Lexer(source).lex()).parse());
        } catch (LangError e) {   //  lexer, parser and interpreter all throw LangError
            System.err.println("Error on line " + e.line + ": " + e.getMessage());    // err = the error stream
        }
    }
}