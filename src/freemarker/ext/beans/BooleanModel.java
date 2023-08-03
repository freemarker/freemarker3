package freemarker.ext.beans;

import freemarker.template.TemplateBooleanModel;

/**
 * <p>A class that will wrap instances of {@link java.lang.Boolean} into a
 * {@link TemplateBooleanModel}.
 * @author Attila Szegedi
 * @version $Id: BooleanModel.java,v 1.8 2003/01/12 23:40:12 revusky Exp $
 */
public class BooleanModel extends BeanModel implements TemplateBooleanModel
{
    private final boolean value;
    
    public BooleanModel(Boolean bool, ObjectWrapper wrapper)
    {
        super(bool, wrapper);
        value = bool.booleanValue();
    }

    public boolean getAsBoolean()
    {
        return value;
    }
}
