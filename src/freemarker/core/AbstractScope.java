package freemarker.core;

import freemarker.template.Template;

/**
 * An abstract scope with no variable storage mechanism of its own; only 
 * provides enclosing scope and operations related to enclosing scope.
 */
public abstract class AbstractScope implements Scope
{
    private final Scope enclosingScope;

    protected AbstractScope(Scope enclosingScope) {
        if (enclosingScope == null) throw new IllegalArgumentException(
                "enclosingScope == null");
        this.enclosingScope = enclosingScope;
    }
    
    public final Scope getEnclosingScope() {
    	return enclosingScope;
    }

    /**
     * Returns the template of the enclosing scope
     * @return the template of the enclosing scope
     */
    public Template getTemplate() {
        return enclosingScope.getTemplate();
    }
    
    /**
     * Returns the Environment of the enclosing scope
     * @return the Environment of the enclosing scope
     */
    public Environment getEnvironment() {
        return enclosingScope.getEnvironment();
    }

    public final Object resolveVariable(String key) {
    	Object result = get(key);
    	if (result == null && enclosingScope != null) {
    		return enclosingScope.resolveVariable(key);
    	}
    	return result;
    }
}
