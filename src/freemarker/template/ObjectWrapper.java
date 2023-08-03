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

    /**
     * The default object wrapper implementation.
     * Wraps Maps as SimpleHash and Lists as SimpleSequences, Strings and 
     * Numbers as SimpleScalar and SimpleNumber respectively.
     * Other objects are beans-wrapped, thus exposing reflection-based information.
     */
    ObjectWrapper DEFAULT_WRAPPER = DefaultObjectWrapper.instance;

    /**
     * Object wrapper that uses SimpleXXX wrappers only.
     * This wrapper has far more restrictive semantics. It 
     * behaves like the DEFAULT_WRAPPER, but for objects
     * that it does not know how to wrap as a SimpleXXX, it 
     * throws an exception. It makes no use of reflection-based 
     * exposure of methods. 
     */
    ObjectWrapper SIMPLE_WRAPPER = SimpleObjectWrapper.instance;
    
    /**
     * @return a TemplateModel wrapper of the object passed in.
     */
    Object wrap(Object obj);
}
