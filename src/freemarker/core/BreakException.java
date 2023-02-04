package freemarker.core;

/**
 * An exception used to break out of a loop
 */
public class BreakException extends RuntimeException {
    private static final long serialVersionUID = -3716099923723830891L;
    public static final BreakException INSTANCE = new BreakException();
}
