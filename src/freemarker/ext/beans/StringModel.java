package freemarker.ext.beans;

import freemarker.ext.util.ModelFactory;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateScalarModel;

/**
 * Subclass of {@link BeanModel} that exposes the return value of the {@link
 * java.lang.Object#toString()} method through the {@link TemplateScalarModel}
 * interface.
 * @author Attila Szegedi
 * @version $Id: StringModel.java,v 1.9 2003/06/03 13:21:33 szegedia Exp $
 */
public class StringModel extends BeanModel
implements TemplateScalarModel
{
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object, BeansWrapper wrapper)
            {
                return new StringModel(object, wrapper);
            }
        };

    /**
     * Creates a new model that wraps the specified object with BeanModel + scalar
     * functionality.
     * @param object the object to wrap into a model.
     * @param wrapper the {@link BeansWrapper} associated with this model.
     * Every model has to have an associated {@link BeansWrapper} instance. The
     * model gains many attributes from its wrapper, including the caching 
     * behavior, method exposure level, method-over-item shadowing policy etc.
     */
    public StringModel(Object object, BeansWrapper wrapper)
    {
        super(object, wrapper);
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
