package org.example.toylang;

// Our own error type. RuntimeException means we don't have to declare "throws" everywhere.
public class LangError extends RuntimeException {
    public final int line;                    // the line the error happened on

    public LangError(String message, int line) {
        super(message);                       // pass the message up so getMessage() works
        this.line = line;                     // remember the line number
    }
}