package freemarker.core.variables;

/**
 * Objects that will be interpreted as true/false in the appropriate
 * context can implement this interface. The truthiness of most objects
 * is already implicit via some rules of thumb, like an empty container
 * is taken to be false.
 */
public interface TemplateBooleanModel {

    /**
     * @return whether to interpret this object as true or false in a boolean context
     */
    boolean getAsBoolean();
}
