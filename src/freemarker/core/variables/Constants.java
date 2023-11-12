package freemarker.core.variables;

import java.util.Iterator;

/**
 * Frequently used constant {@link WrappedVariable} values.
 * 
 * <p>These constants should be stored in the {@link WrappedVariable}
 * sub-interfaces, but for bacward compatibility they are stored here instead.
 * Starting from FreeMarker 2.4 they should be copyed (not moved!) into the
 * {@link WrappedVariable} sub-interfaces, and this class should be marked as
 * deprecated.</p>
 * 
 * @version $Id: Constants.java,v 1.2 2004/11/28 12:58:34 ddekany Exp $
 */
public class Constants {

    /**
     * The type of the {@link JAVA_NULL} object. Using a named 
     * class instead of an anonymous one, as it is easier to figure out what's 
     * wrong from an error message when the reported class name is 
     * "WrappedVariable$JavaNull" than when it is "WrappedVariable$1", also 
     * implements serialization singleton.
     * @author Attila Szegedi
     * @version $Id: $
     */
    public static class JavaNull implements WrappedVariable {
    
        JavaNull() {}
    }

    public static final Iterator<Object> EMPTY_ITERATOR = new Iterator<Object>() {

        public WrappedVariable next() {
            throw new EvaluationException("The collection has no more elements.");
        }

        public boolean hasNext() {
            return false;
        }
        
    };

    public static final Iterable EMPTY_COLLECTION = new Iterable() {

        public Iterator<Object> iterator() {
            return EMPTY_ITERATOR;
        }
        
    };
    
    public static final WrappedHash EMPTY_HASH = new WrappedHash() {

        public int size() {
            return 0;
        }

        public Iterable keys() {
            return EMPTY_COLLECTION;
        }

        public Iterable values() {
            return EMPTY_COLLECTION;
        }

        public WrappedVariable get(String key) {
            return null;
        }

        public boolean isEmpty() {
            return true;
        }
        
    };

    /**
     * A general-purpose object to represent nothing. It acts as
     * an empty string, false, empty sequence, empty hash, and
     * null-returning method. It is useful if you want
     * to simulate typical loose scripting language sorts of 
     * behaviors in your templates. 
     * @deprecated Try not to use this.
     */
    public static final WrappedVariable NOTHING = GeneralPurposeNothing.getInstance();

    /**
     * A singleton value used to represent a java null
     * which comes from a wrapped Java API, for example, i.e.
     * is intentional. A null that comes from a generic container
     * like a map is assumed to be unintentional and a 
     * result of programming error.
     */
    public static final WrappedVariable JAVA_NULL = new Constants.JavaNull();

}
