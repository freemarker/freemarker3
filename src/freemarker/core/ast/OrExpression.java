package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

public class OrExpression extends BooleanExpression {

    private Expression left;
    private Expression right;

    public OrExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
        left.parent = right.parent = this;
    }
    
    public Expression getLeft() {
    	return left;
    }
    
    public Expression getRight() {
    	return right;
    }

    boolean isTrue(Environment env) {
        return left.isTrue(env) || right.isTrue(env);
    }

    boolean isLiteral() {
        return constantValue !=null || (left.isLiteral() && right.isLiteral());
    }

    Expression _deepClone(String name, Expression subst) {
    	return new OrExpression(left.deepClone(name, subst), right.deepClone(name, subst));
    }
}
