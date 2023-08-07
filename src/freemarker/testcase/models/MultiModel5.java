package freemarker.testcase.models;

import freemarker.template.*;
import freemarker.ext.beans.StringModel;
import freemarker.ext.beans.ListModel;

/**
 * Testcase to see how FreeMarker deals with multiple Template models.
 *
 * @version $Id: MultiModel5.java,v 1.12 2004/01/06 17:06:44 szegedia Exp $
 */
public class MultiModel5 implements TemplateSequenceModel, TemplateHashModel {

    private ListModel  listModel = new ListModel();

    /** Creates new MultiModel5 */
    public MultiModel5() {
        listModel.add( new StringModel( "Dummy to make list non-empty" ));
    }

    /**
     * @return the specified index in the list
     */
    public Object get(int i) {
        return listModel.get( i );
    }

    /**
     * @return true if this object is empty.
     */
    public boolean isEmpty() {
        return false;
    }

    public int size() {
        return listModel.size();
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
        if( key.equals( "empty" )) {
            return new StringModel( "Dummy hash value, for test purposes." );
        } else {
            return null;
        }
    }

}
