package freemarker.testcase.models;

import freemarker.template.*;
import freemarker.ext.beans.ListModel;
import freemarker.ext.beans.StringModel;
import freemarker.ext.beans.SimpleMapModel;

/**
 * Testcase to see how FreeMarker deals with multiple Template models.
 *
 * @version $Id: MultiModel1.java,v 1.17 2004/01/06 17:06:44 szegedia Exp $
 */
public class MultiModel1 implements TemplateHashModel,
        TemplateSequenceModel, TemplateScalarModel {

    private TemplateModel m_cSubModel = new MultiModel2();
    private TemplateModel m_cListHashModel1 = new MultiModel4();
    private TemplateModel m_cListHashModel2 = new MultiModel5();
    private TemplateSequenceModel m_cListModel = new ListModel();
    private TemplateHashModel m_cHashModel = new SimpleMapModel();

    /** Creates new MultiModel1 */
    public MultiModel1() {
        for( int i = 0; i < 10; i++ ) {
            ((ListModel)m_cListModel).add( "Model1 value: " + Integer.toString( i ));
        }
        ((ListModel)m_cListModel).add( new MultiModel3() );
        ((SimpleMapModel)m_cHashModel).put( "nested", new MultiModel3() );
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
        if( key.equals( "model2" )) {
            return m_cSubModel;
        } else if( key.equals( "modellist" )) {
            return m_cListModel;
        } else if( key.equals( "selftest" )) {
            return new StringModel( "Selftest of a hash from MultiModel1" );
        } else if( key.equals( "one" )) {
            return m_cListHashModel1;
        } else if( key.equals( "two" )) {
            return m_cListHashModel2;
        } else if( key.equals( "size" )) {
            return new StringModel( "Nasty!" );
        } else if( key.equals( "nesting1" )) {
            return m_cHashModel;
        } else {
            return null;
        }
    }

    /**
     * @return true if this object is empty.
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * @return the specified index in the list
     */
    public Object get(int i) {
        return m_cListModel.get( i );
    }

    /**
     * Returns the scalar's value as a String.
     *
     * @return the String value of this scalar.
     */
    public String getAsString() {
        return "MultiModel1 as a string!";
    }

    public int size() {
        return m_cListModel.size();
    }
}
