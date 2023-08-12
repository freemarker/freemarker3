package freemarker.core.ast;


import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.template.*;
import freemarker.core.parser.ast.ParentheticalExpression;

public class DefaultToExpression extends Expression {
	
	private Expression lhs, rhs;
	
	public DefaultToExpression(Expression lhs, Expression rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		lhs.setParent(this);
		if (rhs != null) rhs.setParent(this);
	}
	
	public Expression getLeft() {
		return lhs;
	}
	
	public Expression getRight() {
		return rhs;
	}

	public Object evaluate(Environment env) {
		Object left = null;		
		try {
			left = lhs.evaluate(env);
		} catch (InvalidReferenceException ire) {
			if (!(lhs instanceof ParentheticalExpression)) {
				throw ire;
			}
		}
		if (left != null && left != Constants.JAVA_NULL) return left;
		if (rhs == null) return Constants.NOTHING;
		return rhs.evaluate(env);
	}

	public Expression _deepClone(String name, Expression subst) {
		if (rhs == null) {
			return new DefaultToExpression(lhs.deepClone(name, subst), null);
		}
		return new DefaultToExpression(lhs.deepClone(name, subst), rhs.deepClone(name, subst));
	}
}
