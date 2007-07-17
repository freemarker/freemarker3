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

	@Override
	Expression _deepClone(String name, Expression subst) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
		return TemplateModel.INVALID_EXPRESSION;
	}

	@Override
	public boolean isLiteral() {
		// TODO Auto-generated method stub
		return false;
	}

}
