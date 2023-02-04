package freemarker.testcase.models;

import freemarker.template.*;

/**
 * Testcase to see how FreeMarker deals with multiple Template models.
 *
 * @version $Id: MultiModel4.java,v 1.13 2004/01/06 17:06:44 szegedia Exp $
 */
public class MultiModel4 implements TemplateSequenceModel, TemplateHashModel {

    private LegacyList m_cList = new LegacyList();

    /**
     * @return the specified index in the list
     */
    public TemplateModel get(int i) throws TemplateModelException {
        return m_cList.get( i );
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
            return new SimpleScalar( "Key size, not the listSize method." );
        } else {
            return null;
        }
    }


    public int size() {
        return m_cList.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

}
