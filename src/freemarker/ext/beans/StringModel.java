package freemarker.ext.beans;

import freemarker.template.TemplateScalarModel;

/**
 * Subclass of {@link Pojo} that exposes the return value of the {@link
 * java.lang.Object#toString()} method through the {@link TemplateScalarModel}
 * interface.
 * @author Attila Szegedi
 * @version $Id: StringModel.java,v 1.9 2003/06/03 13:21:33 szegedia Exp $
 */
public class StringModel extends Pojo implements TemplateScalarModel {
    /**
     * Creates a new model that wraps the specified object with BeanModel + scalar
     * functionality.
     * @param object the object to wrap into a model.
     */
    public StringModel(Object object)
    {
        super(object);
    }

    /**
     * Returns the result of calling {@link Object#toString()} on the wrapped
     * object.
     */
    public String getAsString()
    {
        return object.toString();
    }
}
