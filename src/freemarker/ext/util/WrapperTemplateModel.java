package freemarker.ext.util;

import freemarker.template.TemplateModel;

/**
 * A generic interface for template models that wrap some underlying
 * object, and wish to provide access to the wrapped object.
 * @deprecated use {@link freemarker.template.AdapterTemplateModel} instead.
 * @version $Id: WrapperTemplateModel.java,v 1.9 2005/06/12 19:03:06 szegedia Exp $
 * @author Attila Szegedi
 */
public interface WrapperTemplateModel extends TemplateModel
{
    /**
     * Retrieves the object wrapped by this model.
     */
    public Object getWrappedObject();
}
