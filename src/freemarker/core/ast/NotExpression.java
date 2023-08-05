package freemarker.core.ast;

import freemarker.core.Environment;

public class NotExpression extends BooleanExpression {

    private Expression target;

    public NotExpression(Expression target) {
        this.target = target;
        target.setParent(this);
    }
    
    public Expression getTarget() {
    	return target;
    }

    boolean isTrue(Environment env) {
        return (!target.isTrue(env));
    }

    boolean isLiteral() {
        return target.isLiteral();
    }

    Expression _deepClone(String name, Expression subst) {
    	return new NotExpression(target.deepClone(name, subst));
    }
}
