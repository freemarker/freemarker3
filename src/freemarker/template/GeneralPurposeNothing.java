package freemarker.template;

import java.util.List;
import java.util.Collections;

/**
 * Singleton object representing nothing, used by ?if_exists built-in.
 * It is meant to be interpreted in the most sensible way possible in various contexts.
 * This can be returned to avoid exceptions.
 *
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */

final class GeneralPurposeNothing
implements WrappedBoolean, WrappedString, WrappedSequence, WrappedHash, WrappedMethod {

    private static final WrappedVariable instance = new GeneralPurposeNothing();
      
    private GeneralPurposeNothing() {
    }

    static WrappedVariable getInstance()  {
        return instance;
    }

    public String getAsString() {
        return "";
    }

    public boolean getAsBoolean() {
        return false;
    }

    public boolean isEmpty() {
        return true;
    }

    public int size() {
        return 0;
    }

    public WrappedVariable get(int i) {
        throw new EvaluationException("Empty list");
    }

    public WrappedVariable get(String key) {
        return null;
    }

    public Object exec(List args) {
        return null;
    }
    
    public Iterable keys() {
        return Collections.EMPTY_LIST;
    }

    public Iterable values() {
        return Collections.EMPTY_LIST;
    }
}
