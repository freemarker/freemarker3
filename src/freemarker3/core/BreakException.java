package freemarker3.core;

/**
 * An exception used to break out of a loop
 */
public class BreakException extends RuntimeException {
    public static final BreakException INSTANCE = new BreakException();
}
