package freemarker.core.variables;

import java.util.List;
import java.util.Collections;

/**
 * Singleton object representing nothing, used to implement certain existence 
 * related builtins. It is meant to be interpreted in the most sensible way 
 * possible in various contexts. Basically, this can be returned to avoid exceptions.
 *
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */

final class GeneralPurposeNothing implements Truthy, Sequence, Hash, WrappedMethod {

    private static final Object instance = new GeneralPurposeNothing();
      
    private GeneralPurposeNothing() {
    }

    static Object getInstance()  {
        return instance;
    }

    public String toString() {
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
