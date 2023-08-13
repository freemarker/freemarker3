package freemarker.testcase.models;

import freemarker.template.*;

/**
 * Testcase to see how FreeMarker deals with multiple Template models.
 *
 * @version $Id: MultiModel3.java,v 1.14 2004/01/06 17:06:44 szegedia Exp $
 */
public class MultiModel3 implements TemplateScalarModel, TemplateHashModel {

    /**
     * Returns the scalar's value as a String.
     *
     * @return the String value of this scalar.
     */
    public String getAsString() {
        return "Model3 is alive!";
    }

    /**
     * @return true if this object is empty.
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Gets a <tt>TemplateModel</tt> from the hash.
     *
     * @param key the name by which the <tt>TemplateModel</tt>
     * is identified in the template.
     * @return the <tt>TemplateModel</tt> referred to by the key,
     * or null if not found.
     */
    public Object get(String key) {
        if( key.equals( "selftest" )) {
            return "Selftest from MultiModel3!";
        } else if( key.equals( "message" )) {
            return "Hello world from MultiModel3!";
        } else {
            return null;
        }
    }

}
