package freemarker.template;

/**
 * String values in a template data model must implement this interface.
 * (Actually, the name of this interface should be
 * <code>TemplateStringModel</code>. The bad name was inherited from the
 * ancient times, when there was only 1 kind of scalars in FreeMarker.)
 *
 * @version $Id: TemplateScalarModel.java,v 1.18 2004/11/28 12:58:33 ddekany Exp $
 */
public interface TemplateScalarModel extends TemplateModel {

    /**
     * Returns the string representation of this model. In general, avoid
     * returning null. In compatibility mode the engine will convert
     * null into empty string, however in normal mode it will
     * throw an exception if you return null from this method.
     */
    public String getAsString();
}
