package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.core.parser.ast.Expression;
import freemarker.template.*;

/**
 * A node representing an invalid expression
 * @author revusky
 *
 */

public class InvalidExpression extends Expression {
	
	private String message;
	
	public InvalidExpression(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		if (message==null || message.length() ==0) {
			return "Invalid Expression";
		}
		return message;
	}
	
    public Expression _deepClone(String name, Expression subst) {
    	return this;
    }	

	@Override
	public Object evaluate(Environment env) {
		return Constants.INVALID_EXPRESSION;
	}
}
