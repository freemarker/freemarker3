package freemarker.ext.beans;

import freemarker.template.TemplateNumberModel;

/**
 * Wraps arbitrary subclass of {@link java.lang.Number} into a reflective model.
 * Beside acting as a {@link TemplateNumberModel}, you can call all Java methods on
 * these objects as well.
 */
public class NumberModel extends BeanModel implements TemplateNumberModel
{
    /**
     * Creates a new model that wraps the specified number object.
     * @param number the number object to wrap into a model.
     */
    public NumberModel(Number number)
    {
        super(number);
    }

    public Number getAsNumber()
    {
        return (Number)object;
    }
}
