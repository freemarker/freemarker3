package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.template.*;


public class ExistsExpression extends Expression {
	
	private Expression exp;
	
	
	public ExistsExpression(Expression exp) {
		this.exp = exp;
		exp.setParent(this);
	}
	
	public Expression getExpression() {
		return exp;
	}

	public Object getAsTemplateModel(Environment env) {
		Object tm = null;
		try {
			tm = exp.getAsTemplateModel(env);
		} catch (InvalidReferenceException ire) {
			if (!(exp instanceof ParentheticalExpression)) {
				throw ire;
			}
		}
		return tm == null || tm == Constants.JAVA_NULL 
		       ? TemplateBooleanModel.FALSE 
		       : TemplateBooleanModel.TRUE;
	}

	public Expression _deepClone(String name, Expression subst) {
		return new ExistsExpression(exp.deepClone(name, subst));
	}
}
