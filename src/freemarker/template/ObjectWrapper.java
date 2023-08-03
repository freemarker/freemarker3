package freemarker.template;

import freemarker.ext.beans.BeansWrapper;

/**
 * <p>An object that knows how to "wrap" a java object
 * as a TemplateModel instance.
 *
 * @version $Id: ObjectWrapper.java,v 1.15 2003/06/22 17:50:28 ddekany Exp $
 */
public interface ObjectWrapper {
    /**
     * An ObjectWrapper that works similarly to {@link #SIMPLE_WRAPPER}, but
     * exposes the objects methods and JavaBeans properties as hash elements
     * and custom handling for Java Maps, ResourceBundles, etc.
     */
    ObjectWrapper BEANS_WRAPPER = BeansWrapper.getDefaultInstance();

    ObjectWrapper DEFAULT_WRAPPER = BEANS_WRAPPER;

    ObjectWrapper SIMPLE_WRAPPER = BEANS_WRAPPER;
    
    /**
     * @return a TemplateModel wrapper of the object passed in.
     */
    Object wrap(Object obj);
}
