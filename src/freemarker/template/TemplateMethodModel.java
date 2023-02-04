

/*
 * 22 October 1999: This class added by Holger Arendt.
 */package freemarker.template;

import java.util.List;

import freemarker.core.Environment;

/**
 * Objects that act as methods in a template data model must implement this 
 * interface.
 * @version $Id: TemplateMethodModel.java,v 1.11 2003/09/22 23:56:54 revusky Exp $
 */
public interface TemplateMethodModel extends TemplateModel {

    /**
     * Executes a method call. All arguments passed to the method call are 
     * coerced to strings before being passed, if the FreeMarker rules allow
     * the coercion. If some of the passed arguments can not be coerced to a
     * string, an exception will be raised in the engine and the method will 
     * not be called. If your method would like to act on actual data model 
     * objects instead of on their string representations, implement the 
     * {@link TemplateMethodModelEx} instead.
     * @param arguments a <tt>List</tt> of <tt>String</tt> objects
     * containing the values of the arguments passed to the method. 
     * @return the return value of the method, or null. If the returned value
     * does not implement {@link TemplateModel}, it will be automatically 
     * wrapped using the {@link Environment#getObjectWrapper() environment 
     * object wrapper}.
     */
    public Object exec(List arguments) throws TemplateModelException;
}
