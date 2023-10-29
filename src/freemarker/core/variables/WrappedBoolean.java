package freemarker.core.variables;

/**
 * Objects that will be interpreted as true/false in the appropriate
 * context can implement this interface.
 */
public interface WrappedBoolean extends WrappedVariable {

    /**
     * @return whether to interpret this object as true or false in a boolean context
     */
    boolean getAsBoolean();
}
