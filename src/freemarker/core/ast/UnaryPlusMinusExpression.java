package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.ext.beans.NumberModel;
import freemarker.template.*;

public class UnaryPlusMinusExpression extends Expression {

    private Expression target;
    private boolean isMinus;
    private static final Integer MINUS_ONE = Integer.valueOf(-1); 

    public UnaryPlusMinusExpression(Expression target, boolean isMinus) {
        this.target = target;
        this.isMinus = isMinus;
        target.setParent(this);
    }
    
    public Expression getTarget() {
    	return target;
    }
    
    public boolean isMinus() {
    	return isMinus;
    }
    
    public Object _getAsTemplateModel(Environment env) {
        TemplateNumberModel targetModel = null;
        try {
            targetModel = (TemplateNumberModel) target.getAsTemplateModel(env);
        } catch (ClassCastException cce) {
            String msg = "Error " + getStartLocation();
            msg += "\nExpression " + target + " is not numerical.";
            throw new NonNumericalException(msg, env);
        }
        if (!isMinus) {
            return targetModel;
        }
        Number n = targetModel.getAsNumber();
        n = ArithmeticEngine.CONSERVATIVE_ENGINE.multiply(MINUS_ONE, n);
        return new NumberModel(n);
    }

    public boolean isLiteral() {
        return target.isLiteral();
    }

    public Expression _deepClone(String name, Expression subst) {
    	return new UnaryPlusMinusExpression(target.deepClone(name, subst), isMinus);
    }
}
