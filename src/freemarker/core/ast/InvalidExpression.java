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
	
    Expression _deepClone(String name, Expression subst) {
    	return this;
    }	

	@Override
	TemplateModel _getAsTemplateModel(Environment env) {
		return TemplateModel.INVALID_EXPRESSION;
	}

	@Override boolean isLiteral() {
		// TODO Auto-generated method stub
		return false;
	}

}
