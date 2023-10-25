package freemarker.testcase.models;

import freemarker.template.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Testcase to see how FreeMarker deals with multiple Template models.
 *
 * @version $Id: MultiModel5.java,v 1.12 2004/01/06 17:06:44 szegedia Exp $
 */
public class MultiModel5 implements WrappedSequence, WrappedHash {

    private List  list = new ArrayList();

    /** Creates new MultiModel5 */
    public MultiModel5() {
        list.add("Dummy to make list non-empty");
    }

    /**
     * @return the specified index in the list
     */
    public Object get(int i) {
        return list.get( i );
    }

    /**
     * @return true if this object is empty.
     */
    public boolean isEmpty() {
        return false;
    }

    public int size() {
        return list.size();
    }

    /**
     * Gets a <tt>WrappedVariable</tt> from the hash.
     *
     * @param key the name by which the <tt>WrappedVariable</tt>
     * is identified in the template.
     * @return the <tt>WrappedVariable</tt> referred to by the key,
     * or null if not found.
     */
    public Object get(String key) {
        if( key.equals( "empty" )) {
            return "Dummy hash value, for test purposes.";
        } 
        return null;
    }

}
