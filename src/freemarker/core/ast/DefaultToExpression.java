package freemarker.core.ast;


import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.template.*;
import freemarker.core.parser.ast.ParentheticalExpression;

public class DefaultToExpression extends Expression {
	
	public DefaultToExpression() {}
	
	public DefaultToExpression(Expression lhs, Expression rhs) {
		add(lhs);
		if (rhs!=null) add(rhs);
	}
	
	public Expression getLeft() {
		return (Expression) get(0);
	}
	
	public Expression getRight() {
		return childrenOfType(Expression.class).size() == 2 ?
		(Expression) getLastChild() : null;
	}

	public Object evaluate(Environment env) {
		Object left = null;		
		try {
			left = getLeft().evaluate(env);
		} catch (InvalidReferenceException ire) {
			if (!(getLeft() instanceof ParentheticalExpression)) {
				throw ire;
			}
		}
		if (left != null && left != Constants.JAVA_NULL) return left;
		if (getRight() == null) return Constants.NOTHING;
		return getRight().evaluate(env);
	}

	public Expression _deepClone(String name, Expression subst) {
		DefaultToExpression result = new DefaultToExpression();
		result.add(getLeft().deepClone(name, subst));
		result.add(get(1));
		if (getRight() != null) {
			result.add(getRight().deepClone(name, subst));
		}
		return result;
	}
}
