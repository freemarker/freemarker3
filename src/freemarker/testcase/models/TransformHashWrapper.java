package freemarker.testcase.models;

import freemarker.template.*;
import freemarker.template.utility.*;

/**
 * Part of the TestTransform testcase suite.
 *
 * @version $Id: TransformHashWrapper.java,v 1.15 2005/06/16 18:13:59 ddekany Exp $
 */
public class TransformHashWrapper implements TemplateHashModel,
        TemplateScalarModel {

    private SimpleHash m_cHashModel = new SimpleHash();

    /** Creates new TransformHashWrapper */
    public TransformHashWrapper() {
        m_cHashModel.put( "htmlEscape", new HtmlEscape() );
        m_cHashModel.put( "compress", new StandardCompress() );
        m_cHashModel.put( "escape", new TransformMethodWrapper1() );
        m_cHashModel.put( "special", new TransformMethodWrapper2() );
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
        return m_cHashModel.get( key );
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
    public String getAsString() {
        return "Utility transformations";
    }
}
