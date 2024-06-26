package freemarker3.core.variables.scope;

import freemarker3.template.TemplateException;

/**
 * This exception is thrown when a set directive in the template
 * tries to set a variable which is not declared in that scope or 
 * an enclosing one. (Note: This only applies if the template 
 * contains at least one top-level var directive. 
 * @author revusky
 */

public class UndeclaredVariableException extends TemplateException {

	public UndeclaredVariableException(String message) {
		super(message);
	}
}
