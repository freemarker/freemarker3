package freemarker.template;

/**
 * <p>The default implementation of the ObjectWrapper
 * interface.
 *
 * @version $Id: SimpleObjectWrapper.java,v 1.24 2003/05/30 16:29:44 szegedia Exp $
 */
public class SimpleObjectWrapper extends DefaultObjectWrapper {
    
    static final SimpleObjectWrapper instance = new SimpleObjectWrapper();
    
    /**
     * Called if a type other than the simple ones we know about is passed in. 
     * In this implementation, this just throws an exception.
     */
    protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException {
        throw new TemplateModelException("Don't know how to present an object of this type to a template: " 
                                         + obj.getClass().getName());
    }
}
