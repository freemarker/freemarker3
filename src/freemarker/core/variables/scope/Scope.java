package freemarker.core.variables.scope;

import freemarker.template.Template;
import freemarker.core.Environment;

/**
 * Represents a variable resolution context in FTL. This 
 * may be the local variables in a macro, the context of a loop
 * or a template namespace 
 * @author Jonathan Revusky
 */
public interface Scope {

    default Object resolveVariable(String key) {
    	Object result = get(key);
    	if (result == null && getEnclosingScope() != null) {
    		return getEnclosingScope().resolveVariable(key);
    	}
    	return result;
    }

    default Template getTemplate() {
        return getEnclosingScope().getTemplate();
    }

    default Environment getEnvironment() {
        return getEnclosingScope().getEnvironment();
    }

    /**
     * Gets a variable from the hash.
     *
     * @param key the name by which the <tt>WrappedVariable</tt>
     * is identified in the template.
     * @return the value referred to by the key,
     * or null if not found.
     */
    Object get(String key);

    /**
     * Set a variable in this scope. This 
     * will typically only be used internally by the FreeMarker engine.
     */
    Object put(String key, Object value);

    /**
     * Removes a variable in this scope.
     * This will typically only be used by FreeMarker engine internals 
     */
    Object remove(String key);

    /**
     * @return whether the variable is defined in
     * this specific scope. (It could be defined in a 
     * fallback scope and this method will return false.)
     */
    boolean definesVariable(String name);

    /**
     * @return the fallback Scope for variable resolution
     */
    Scope getEnclosingScope();

    default boolean isTemplateNamespace() {
        return false;
    }
}
