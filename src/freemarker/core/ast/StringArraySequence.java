package freemarker.core.ast;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

/**
 * Sequence variable implementation that wraps a String[] with relatively low
 * resource utilization. Warning: it does not copy the wrapped array, so do
 * not modify that after the model was made!
 *
 * @author Daniel Dekany
 * @version $Id: StringArraySequence.java,v 1.2 2004/01/06 17:06:42 szegedia Exp $
 */
public class StringArraySequence implements TemplateSequenceModel {
    private String[] stringArray;
    private TemplateScalarModel[] array;

    /**
     * Warning: Does not copy the argument array!
     */
    public StringArraySequence(String[] stringArray) {
        this.stringArray = stringArray;
    }

    public TemplateModel get(int index) {
        if (array == null) {
            array = new TemplateScalarModel[stringArray.length];
        }
        TemplateScalarModel result = array[index];
        if (result == null) {
            result = new StringModel(stringArray[index]);
            array[index] = result;
        }
        return result;
    }

    public int size() {
        return stringArray.length;
    }
}
