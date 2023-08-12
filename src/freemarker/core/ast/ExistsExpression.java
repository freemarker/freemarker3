package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.parser.ast.ParentheticalExpression;
import static freemarker.template.Constants.JAVA_NULL;


public class ExistsExpression extends Expression {
	
	private Expression exp;
	
	
	public ExistsExpression(Expression exp) {
		this.exp = exp;
		exp.setParent(this);
	}
	
	public Expression getExpression() {
		return exp;
	}

	public Object evaluate(Environment env) {
		Object tm = null;
		try {
			tm = exp.evaluate(env);
		} catch (InvalidReferenceException ire) {
			if (!(exp instanceof ParentheticalExpression)) {
				throw ire;
			}
		}
		return tm != null && tm != JAVA_NULL;
	}

	public Expression _deepClone(String name, Expression subst) {
		return new ExistsExpression(exp.deepClone(name, subst));
	}
}
