package freemarker.core;

/**
 * This exception is thrown when a set directive in the template
 * tries to set a variable which is not declared in that scope or 
 * an enclosing one. (Note: This only applies if the template 
 * contains at least one top-level var directive. 
 * @author revusky
 */

public class UndeclaredVariableException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7809054327560058509L;

	public UndeclaredVariableException(String message) {
		super(message);
	}
}
