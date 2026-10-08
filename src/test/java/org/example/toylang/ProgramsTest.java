// use of ai as a step by step learing approach due to this being new for where I am in my studies
package org.example.toylang;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProgramsTest {

    // runs a program file through Main and returns everything it printed (output and errors)
    private String run(String path) throws Exception {
        PrintStream oldOut = System.out;                    // remember the real streams
        PrintStream oldErr = System.err;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();   // a place to collect output
        PrintStream capture = new PrintStream(buffer);
        System.setOut(capture);                             // redirect printing into the buffer
        System.setErr(capture);
        try {
            Main.main(new String[]{path});                  // run the program
        } finally {
            System.setOut(oldOut);                          // always put the real streams back
            System.setErr(oldErr);
        }
        return buffer.toString().trim().replace("\r\n", "\n");   // tidy line endings (Windows uses \r\n)
    }

    @Test
    void factorialPrints120() throws Exception {
        assertEquals("120.0", run("programs/factorial.toy"));
    }

    @Test
    void countdownPrintsThreeTwoOne() throws Exception {
        assertEquals("3.0\n2.0\n1.0", run("programs/countdown.toy"));
    }

    @Test
    void sumPrints55() throws Exception {
        assertEquals("55.0", run("programs/sum.toy"));
    }

    @Test
    void ifElsePrintsOne() throws Exception {
        assertEquals("1.0", run("programs/ifelse.toy"));
    }

    @Test
    void divisionByZeroReportsError() throws Exception {
        assertEquals("Error on line 1: Division by zero", run("programs/error.toy"));
    }
}