package freemarker.core.evaluation;

/**
 * Numeric values in a template data model must implement this interface.
 *
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 *
 * @version $Id: WrappedNumber.java,v 1.14 2004/11/27 14:49:57 ddekany Exp $
 */
public interface WrappedNumber extends WrappedVariable {

    /**
     * Returns the numeric value. The return value must not be null.
     *
     * @return the {@link Number} instance associated with this number model.
     */
    public Number getAsNumber();
    
}
