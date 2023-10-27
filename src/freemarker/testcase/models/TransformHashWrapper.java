package freemarker.testcase.models;

import freemarker.template.*;
import freemarker.ext.beans.SimpleMapModel;
import freemarker.template.utility.*;

/**
 * Part of the TestTransform testcase suite.
 *
 * @version $Id: TransformHashWrapper.java,v 1.15 2005/06/16 18:13:59 ddekany Exp $
 */
public class TransformHashWrapper implements WrappedHash {

    private SimpleMapModel hash = new SimpleMapModel();

    /** Creates new TransformHashWrapper */
    public TransformHashWrapper() {
        hash.put( "htmlEscape", new HtmlEscape() );
        hash.put( "compress", new StandardCompress() );
        hash.put( "escape", new TransformMethodWrapper1() );
        hash.put( "special", new TransformMethodWrapper2() );
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
        return hash.get( key );
    }

    /**
     * @return true if this object is empty.
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns the scalar's value as a String.
     * @return the String value of this scalar.
     */
    public String toString() {
        return "Utility transformations";
    }
}
