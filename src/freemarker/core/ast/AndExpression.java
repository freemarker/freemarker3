package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

public class AndExpression extends BooleanExpression {

    private Expression left;
    private Expression right;

    public AndExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
        left.parent = this;
        right.parent = this;
    }
    
    public Expression getLeft() {
    	return left;
    }
    
    public Expression getRight() {
    	return right;
    }

    boolean isTrue(Environment env) {
        return left.isTrue(env) && right.isTrue(env);
    }

    boolean isLiteral() {
        return constantValue != null || (left.isLiteral() && right.isLiteral());
    }

    Expression _deepClone(String name, Expression subst) {
    	return new AndExpression(left.deepClone(name, subst), right.deepClone(name, subst));
    }
}