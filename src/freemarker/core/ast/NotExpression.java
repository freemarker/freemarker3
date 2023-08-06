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

    public boolean isTrue(Environment env) {
        return (!target.isTrue(env));
    }

    public boolean isLiteral() {
        return target.isLiteral();
    }

    public Expression _deepClone(String name, Expression subst) {
    	return new NotExpression(target.deepClone(name, subst));
    }
}
