package freemarker.template;

import java.util.List;

import freemarker.core.Environment;
import freemarker.template.utility.DeepUnwrap;

/**
 * A subinterface of {@link TemplateMethodModel} that acts on models, rather
 * than on strings. {@link TemplateMethodModel} interface will receive string
 * representations of its argument expressions, while this interface receives
 * the models themselves. The interface has no new methods. Instead, by 
 * implementing this interface the class declares that it wishes to receive 
 * actual TemplateModel instances in its arguments list when invoked instead of
 * their string representations. Further, if the implementation wishes to 
 * operate on POJOs that might be underlying the models, it can use the static 
 * utility methods in the {@link DeepUnwrap} class to easily obtain them.
 * @author Attila Szegedi, szegedia at users dot sourceforge dot net
 * @version $Id: TemplateMethodModelEx.java,v 1.8 2003/01/12 23:40:21 revusky Exp $
 */
public interface TemplateMethodModelEx extends TemplateMethodModel {

    /**
     * Executes a method call. 
     * @param arguments a <tt>List</tt> of {@link TemplateModel} objects
     * containing the values of the arguments passed to the method. If the 
     * implementation wishes to operate on POJOs that might be underlying the 
     * models, it can use the static utility methods in the {@link DeepUnwrap} 
     * class to easily obtain them.
     * @return the return value of the method, or null. If the returned value
     * does not implement {@link TemplateModel}, it will be automatically 
     * wrapped using the {@link Environment#getObjectWrapper() environment 
     * object wrapper}.
     */
    public Object exec(List arguments) throws TemplateModelException;
}