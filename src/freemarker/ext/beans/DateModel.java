package freemarker.ext.beans;

import java.util.Date;

import freemarker.ext.util.ModelFactory;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModel;

/**
 * Wraps arbitrary subclass of {@link java.util.Date} into a reflective model.
 * Beside acting as a {@link TemplateDateModel}, you can call all Java methods
 * on these objects as well.
 */
public class DateModel
extends
    BeanModel
implements
    TemplateDateModel
{
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object, ObjectWrapper wrapper)
            {
                return new DateModel((Date)object, (BeansWrapper)wrapper);
            }
        };

    private final int type;
    
    /**
     * Creates a new model that wraps the specified date object.
     * @param date the date object to wrap into a model.
     * @param wrapper the {@link BeansWrapper} associated with this model.
     * Every model has to have an associated {@link BeansWrapper} instance. The
     * model gains many attributes from its wrapper, including the caching 
     * behavior, method exposure level, method-over-item shadowing policy etc.
     */
    public DateModel(Date date, BeansWrapper wrapper)
    {
        super(date, wrapper);
        if(date instanceof java.sql.Date) {
            type = DATE;
        }
        else if(date instanceof java.sql.Time) {
            type = TIME;
        }
        else if(date instanceof java.sql.Timestamp) {
            type = DATETIME;
        }
        else {
            type = wrapper.getDefaultDateType();
        }
    }

    public Date getAsDate() {
        return (Date)object;
    }

    public int getDateType() {
        return type;
    }
}
