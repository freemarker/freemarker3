package freemarker.testcase.models;

import freemarker.template.*;
import freemarker.ext.beans.ListModel;
import freemarker.ext.beans.StringModel;

/**
 * Testcase to see how FreeMarker deals with multiple Template models.
 *
 * @version $Id: MultiModel4.java,v 1.13 2004/01/06 17:06:44 szegedia Exp $
 */
public class MultiModel4 implements TemplateSequenceModel, TemplateHashModel {

    private ListModel listModel = new ListModel();

    /**
     * @return the specified index in the list
     */
    public Object get(int i) {
        return listModel.get( i );
    }

    /**
     * Gets a <tt>TemplateModel</tt> from the hash.
     *
     * @param key the name by which the <tt>TemplateModel</tt>
     * is identified in the template.
     * @return the <tt>TemplateModel</tt> referred to by the key,
     * or null if not found.
     */
    public TemplateModel get(String key) {
        if( key.equals( "size" )) {
            return new StringModel( "Key size, not the listSize method." );
        } else {
            return null;
        }
    }


    public int size() {
        return listModel.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

}
