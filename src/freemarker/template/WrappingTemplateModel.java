package freemarker.template;

import java.beans.Beans;

import freemarker.ext.beans.ObjectWrapper;

/**
 * A base class for containers that wrap arbitrary Java objects into 
 * {@link TemplateModel} instances.
 *
 * @version $Id: WrappingTemplateModel.java,v 1.19 2006/03/15 17:46:23 revusky Exp $
 */
abstract public class WrappingTemplateModel {

    private static ObjectWrapper defaultObjectWrapper = ObjectWrapper.getDefaultInstance();
    private ObjectWrapper objectWrapper = ObjectWrapper.getDefaultInstance();
    
    /**
     * Sets the default object wrapper that is used when a wrapping template
     * model is constructed without being passed an explicit object wrapper.
     * Note that {@link Configuration#setSharedVariable(String, Object)} and
     * {@link Template#process(Object, java.io.Writer)} don't use this setting,
     * they rather use whatever object wrapper their 
     * {@link Configuration#getObjectWrapper()} method returns.
     */
    public static void setDefaultObjectWrapper(ObjectWrapper objectWrapper) {
        defaultObjectWrapper = objectWrapper;
    }

    /**
     * Returns the default object wrapper that is used when a wrapping template
     * model is constructed without being passed an explicit object wrapper.
     * Note that {@link Configuration#setSharedVariable(String, Object)} and
     * {@link Template#process(Object, java.io.Writer)} don't use this setting,
     * they rather use whatever object wrapper their 
     * {@link Configuration#getObjectWrapper()} method returns.
     */
    public static ObjectWrapper getDefaultObjectWrapper() {
        return defaultObjectWrapper;
    }
    
    /**
     * Protected constructor that creates a new wrapping template model using
     * the default object wrapper.
     */
    protected WrappingTemplateModel() {
        this(defaultObjectWrapper);
    }

    /**
     * Protected constructor that creates a new wrapping template model using
     * the specified object wrapper.
     * @param objectWrapper the wrapper to use. If null is passed, the default
     * object wrapper is used.
     */
    protected WrappingTemplateModel(ObjectWrapper objectWrapper) {
        this.objectWrapper = ObjectWrapper.getDefaultInstance();
    }
    
    /**
     * Returns the object wrapper instance used by this wrapping template model.
     */
    public ObjectWrapper getObjectWrapper() {
        return objectWrapper;
    }

    public void setObjectWrapper(ObjectWrapper objectWrapper) {
        this.objectWrapper = objectWrapper;
    }

    /**
     * Wraps the passed object into a template model using this object's object
     * wrapper, except that null is not wrapped but simply returned.
     * @param obj the object to wrap
     * @return the template model that wraps the object
     * @throws TemplateModelException if the wrapper does not know how to
     * wrap the passed object.
     */
    protected final Object wrap(Object obj) {
//    	if (obj == null) return null;
        return objectWrapper.wrap(obj);
    }
}
