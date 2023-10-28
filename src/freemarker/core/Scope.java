package freemarker.core;

import java.util.Collection;

import freemarker.template.*;

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
     * Returns the names of variables directly managed by this scope (i.e. it 
     * does not traverse the chain of enclosing scopes, but limits itself to 
     * this scope only).
     * @return a collection of known variable names for this scope, without
     * enclosing scopes. The returned collection should be either immutable, or
     * it should be disconnected from the scope, so any modifications to the
     * collection don't affect the scope.
     * @throws EvaluationException
     */
    Collection<String> getDirectVariableNames();

    /**
     * Gets a variable from the hash.
     *
     * @param key the name by which the <tt>WrappedVariable</tt>
     * is identified in the template.
     * @return the value referred to by the key,
     * or null if not found.
     */
    Object get(String key);
}
