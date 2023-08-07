package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

public class OrExpression extends BooleanExpression {

    private Expression left;
    private Expression right;

    public OrExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
        left.setParent(this);
        right.setParent(this);
    }
    
    public Expression getLeft() {
    	return left;
    }
    
    public Expression getRight() {
    	return right;
    }

    public boolean isTrue(Environment env) {
        return left.isTrue(env) || right.isTrue(env);
    }

    public Expression _deepClone(String name, Expression subst) {
    	return new OrExpression(left.deepClone(name, subst), right.deepClone(name, subst));
    }
}
