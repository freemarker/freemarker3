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

    /**
     * Set a variable in this scope. This 
     * will typically only be used internally by the FreeMarker engine.
     */
    void put(String key, Object value);

    /**
     * Removes a variable in this scope.
     * This will typically only be used by FreeMarker engine internals 
     */
    Object remove(String key);

    /**
     * @return the Environment object associated with this Scope.
     */

    Environment getEnvironment();

    /**
     * @return the Template object associated with this Scope.
     */
    Template getTemplate();

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


    /**
     * Evaluates the variable of this name in this scope,
     * falling back to the enclosing Scope if it is not
     * defined in this one.
     */
    Object resolveVariable(String name);

    /**
     * Gets a variable from the hash.
     *
     * @param key the name by which the <tt>WrappedVariable</tt>
     * is identified in the template.
     * @return the value referred to by the key,
     * or null if not found.
     */
    Object get(String key);

    default boolean isTemplateNamespace() {
        return false;
    }
}
