package freemarker.template;

import java.util.List;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateSequenceModel;

/**
 * Sequence that wraps a <code>java.util.List</code> of already wrapped objects
 * directly, with minimal resource usage. Warning! It does not copy the original
 * list.
 *
 * @author Daniel Dekany
 * @version $Id: TemplateModelListSequence.java,v 1.2 2004/01/06 17:06:43 szegedia Exp $
 */
public class TemplateModelListSequence implements TemplateSequenceModel {
    private List list;

    public TemplateModelListSequence(List list) {
        this.list = list;
    }

    public TemplateModel get(int index) {
        return (TemplateModel) list.get(index);
    }

    public int size() {
        return list.size();
    }

    public Object getWrappedObject() {
        return list;
    }
}
