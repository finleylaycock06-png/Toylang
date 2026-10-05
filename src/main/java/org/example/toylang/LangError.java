package org.example.toylang;

public class LangError extends RuntimeException {
    public final int line;

    public LangError(String message, int line){
        super(message);
        this.line = line;
    }
}

