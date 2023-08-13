package freemarker.testcase.models;

import freemarker.template.*;

/**
 * Tests the impact that the isEmpty() has on template hash models.
 *
 * @author  <a href="mailto:run2000@users.sourceforge.net">Nicholas Cull</a>
 * @version $Id: BooleanHash1.java,v 1.15 2004/01/06 17:06:44 szegedia Exp $
 */
public class BooleanHash1 implements TemplateHashModel {

    /**
     * Gets a <tt>TemplateModel</tt> from the hash.
     *
     * @param key the name by which the <tt>TemplateModel</tt>
     * is identified in the template.
     * @return the <tt>TemplateModel</tt> referred to by the key,
     * or null if not found.
     */
    public Object get(String key) {
        if( key.equals( "temp" )) {
            return "Hello, world.";
        } else if( key.equals( "boolean" )) {
            return false;
        } else {
            return "Just another key...";
        }
    }

    /**
     * @return true if this object is empty.
     */
    public boolean isEmpty() {
        return true;
    }
}
