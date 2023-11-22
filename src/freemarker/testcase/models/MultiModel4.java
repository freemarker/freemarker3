package freemarker.testcase.models;

import freemarker.core.variables.*;
import freemarker.template.TemplateSequenceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Testcase to see how FreeMarker deals with multiple Template models.
 *
 * @version $Id: MultiModel4.java,v 1.13 2004/01/06 17:06:44 szegedia Exp $
 */
public class MultiModel4 implements TemplateSequenceModel, Hash {

    private List list = new ArrayList();

    /**
     * @return the specified index in the list
     */
    public Object get(int i) {
        return list.get(i);
    }

    /**
     * Gets a <tt>WrappedVariable</tt> from the hash.
     *
     * @param key the name by which the value
     * is identified in the template.
     * @return the value referred to by the key,
     * or null if not found.
     */
    public Object get(String key) {
        if( key.equals( "size" )) {
            return "Key size, not the listSize method.";
        } 
        return null;
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

}
