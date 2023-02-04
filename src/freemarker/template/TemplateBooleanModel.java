package freemarker.template;

/**
 * Objects that will be interpreted as true/false in the appropriate
 * context must implement this interface.
 *
 * @version $Id: TemplateBooleanModel.java,v 1.8 2003/01/12 23:40:21 revusky Exp $
 */
public interface TemplateBooleanModel extends TemplateModel {

    /**
     * @return whether to interpret this object as true or false in a boolean context
     */

    boolean getAsBoolean() throws TemplateModelException;
    /**
     * A singleton object to represent boolean false
     */
    TemplateBooleanModel FALSE = new TemplateBooleanModel() {
        public boolean getAsBoolean() {
            return false;
        }
        private Object readResolve() {
            return FALSE;
        }
    };

    /**
     * A singleton object to represent boolean true
     */
    TemplateBooleanModel TRUE = new TemplateBooleanModel() {
        public boolean getAsBoolean() {
            return true;
        }
        private Object readResolve() {
            return TRUE;
        }
    };
}
