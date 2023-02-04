package freemarker.ext.beans;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Utility class for instantiating models for representing static methods of
 * Java classes from templates. If your template's data model contains an 
 * instance of StaticModels (named, say <tt>StaticModels</tt>), then you can
 * instantiate an arbitrary StaticModel using get syntax (i.e.
 * <tt>StaticModels["java.lang.System"].currentTimeMillis()</tt>).
 * @author Attila Szegedi
 * @version $Id: StaticModels.java,v 1.13 2005/11/03 08:49:19 szegedia Exp $
 */
class StaticModels extends ClassBasedModelFactory {
    
    StaticModels(BeansWrapper wrapper) {
        super(wrapper);
    }

    protected TemplateModel createModel(Class clazz) 
    throws TemplateModelException {
        return new StaticModel(clazz, getWrapper());
    }
}