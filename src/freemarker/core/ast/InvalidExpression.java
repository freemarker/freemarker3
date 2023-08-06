package freemarker.core.ast;

import freemarker.core.Environment;
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
	public Object _getAsTemplateModel(Environment env) {
		return Constants.INVALID_EXPRESSION;
	}

	@Override public boolean isLiteral() {
		// TODO Auto-generated method stub
		return false;
	}

}
