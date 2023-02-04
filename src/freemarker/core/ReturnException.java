package freemarker.core;

/**
 * An exception used to jump out of a macro or function
 */

public class ReturnException extends RuntimeException {
    private static final long serialVersionUID = 7681800136354585466L;

    public static final ReturnException INSTANCE = new ReturnException();
    private ReturnException() {
    }
}
