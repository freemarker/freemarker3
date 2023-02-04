package freemarker.testcase.models;

import freemarker.template.*;

/**
 * Model for testing the impact of isEmpty() on template list models. Every
 * other method simply delegates to a SimpleList model.
 *
 * @author  <a href="mailto:run2000@users.sourceforge.net">Nicholas Cull</a>
 * @version $Id: BooleanList1.java,v 1.16 2004/01/06 17:06:44 szegedia Exp $
 */
public class BooleanList1 implements TemplateSequenceModel {

    private LegacyList  cList;

    /** Creates new BooleanList1 */
    public BooleanList1() {
        cList = new LegacyList();
        cList.add( "false" );
        cList.add( "0" );
        cList.add(TemplateBooleanModel.FALSE);
        cList.add(TemplateBooleanModel.TRUE);
        cList.add(TemplateBooleanModel.TRUE);
        cList.add(TemplateBooleanModel.TRUE);
        cList.add(TemplateBooleanModel.FALSE);
    }

    /**
     * @return true if there is a next element.
     */
    public boolean hasNext() {
        return cList.hasNext();
    }

    /**
     * @return the next element in the list.
     */
    public TemplateModel next() throws TemplateModelException {
        return cList.next();
    }

    /**
     * @return true if the cursor is at the beginning of the list.
     */
    public boolean isRewound() {
        return cList.isRewound();
    }

    /**
     * @return the specified index in the list
     */
    public TemplateModel get(int i) throws TemplateModelException {
        return cList.get(i);
    }

    /**
     * Resets the cursor to the beginning of the list.
     */
    public void rewind() {
        cList.rewind();
    }

    public int size() {
        return cList.size();
    }

}
