package freemarker.testcase.models;

import freemarker.template.*;

/**
 * Tests the impact that the isEmpty() has on template hash models.
 *
 * @author  <a href="mailto:run2000@users.sourceforge.net">Nicholas Cull</a>
 * @version $Id: BooleanHash2.java,v 1.12 2004/01/06 17:06:44 szegedia Exp $
 */
public class BooleanHash2 implements WrappedHash {

    /**
     * Gets a <tt>WrappedVariable</tt> from the hash.
     *
     * @param key the name by which the <tt>WrappedVariable</tt>
     * is identified in the template.
     * @return the <tt>WrappedVariable</tt> referred to by the key,
     * or null if not found.
     */
    public WrappedVariable get(String key) {
        return null;
    }

    /**
     * @return true if this object is empty.
     */
    public boolean isEmpty() {
        return false;
    }
}
