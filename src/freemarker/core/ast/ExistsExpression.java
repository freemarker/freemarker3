package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.template.*;


public class ExistsExpression extends Expression {
	
	private Expression exp;
	
	
	public ExistsExpression(Expression exp) {
		this.exp = exp;
		exp.parent = this;
	}
	
	public Expression getExpression() {
		return exp;
	}

	TemplateModel _getAsTemplateModel(Environment env) {
		TemplateModel tm = null;
		try {
			tm = exp.getAsTemplateModel(env);
		} catch (InvalidReferenceException ire) {
			if (!(exp instanceof ParentheticalExpression)) {
				throw ire;
			}
		}
		return tm == null || tm == TemplateModel.JAVA_NULL 
		       ? TemplateBooleanModel.FALSE 
		       : TemplateBooleanModel.TRUE;
	}

	boolean isLiteral() {
		return exp.isLiteral();
	}

	Expression _deepClone(String name, Expression subst) {
		return new ExistsExpression(exp.deepClone(name, subst));
	}
}
