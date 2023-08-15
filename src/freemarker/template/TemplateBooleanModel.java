package freemarker.template;

/**
 * Objects that will be interpreted as true/false in the appropriate
 * context can implement this interface.
 */
public interface TemplateBooleanModel extends TemplateModel {

    /**
     * @return whether to interpret this object as true or false in a boolean context
     */
    boolean getAsBoolean();
}
