package freemarker.ext.beans;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateNumberModel;

/**
 * Wraps arbitrary subclass of {@link java.lang.Number} into a reflective model.
 * Beside acting as a {@link TemplateNumberModel}, you can call all Java methods on
 * these objects as well.
 */
public class NumberModel
extends
    BeanModel
implements
    TemplateNumberModel
{
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object, ObjectWrapper wrapper)
            {
                return new NumberModel((Number)object, wrapper);
            }
        };
    /**
     * Creates a new model that wraps the specified number object.
     * @param number the number object to wrap into a model.
     * @param wrapper the {@link ObjectWrapper} associated with this model.
     * Every model has to have an associated {@link ObjectWrapper} instance. The
     * model gains many attributes from its wrapper, including the caching 
     * behavior, method exposure level, method-over-item shadowing policy etc.
     */
    public NumberModel(Number number, ObjectWrapper wrapper)
    {
        super(number, wrapper);
    }

    public Number getAsNumber()
    {
        return (Number)object;
    }
}
