package freemarker.template;

/**
 * Numeric values in a template data model must implement this interface.
 *
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 *
 * @version $Id: TemplateNumberModel.java,v 1.14 2004/11/27 14:49:57 ddekany Exp $
 */
public interface TemplateNumberModel extends TemplateModel {

    /**
     * Returns the numeric value. The return value must not be null.
     *
     * @return the {@link Number} instance associated with this number model.
     */
    public Number getAsNumber();
    
}
