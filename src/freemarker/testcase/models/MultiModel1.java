package freemarker.testcase.models;

import freemarker.core.variables.*;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateSequenceModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Testcase to see how FreeMarker deals with multiple Template models.
 *
 * @version $Id: MultiModel1.java,v 1.17 2004/01/06 17:06:44 szegedia Exp $
 */
public class MultiModel1 implements TemplateHashModel, TemplateSequenceModel {

    private Object m_cSubModel = new MultiModel2();
    private Object m_cListHashModel1 = new MultiModel4();
    private Object m_cListHashModel2 = new MultiModel5();
    private List<Object> m_cListModel = new ArrayList<>();
    private Map<String,Object> m_cHashModel = new HashMap<>();

    /** Creates new MultiModel1 */
    public MultiModel1() {
        for( int i = 0; i < 10; i++ ) {
            m_cListModel.add( "Model1 value: " + Integer.toString( i ));
        }
        m_cListModel.add( new MultiModel3() );
        m_cHashModel.put( "nested", new MultiModel3() );
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
        if( key.equals( "model2" )) {
            return m_cSubModel;
        } else if( key.equals( "modellist" )) {
            return m_cListModel;
        } else if( key.equals( "selftest" )) {
            return "Selftest of a hash from MultiModel1";
        } else if( key.equals( "one" )) {
            return m_cListHashModel1;
        } else if( key.equals( "two" )) {
            return m_cListHashModel2;
        } else if( key.equals( "size" )) {
            return "Nasty!";
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
    public String toString() {
        return "MultiModel1 as a string!";
    }

    public int size() {
        return m_cListModel.size();
    }
}
