package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;

/**
 * A class that represents a Range between two integers.
 */
public class Range extends Expression {

    private Expression left;
    private Expression right;

    public Range(Expression left, Expression right) {
        this.left = left;
        this.right = right;
        left.parent = this;
        if (right != null) right.parent = this;
    }
    
    public Expression getLeft() {
    	return left;
    }
    
    public Expression getRight() {
    	return right;
    }
    
    boolean hasRhs() {
        return right != null;
    }

    TemplateModel _getAsTemplateModel(Environment env) 
        throws TemplateException
    {
        int min = EvaluationUtil.getNumber(left, env).intValue();
        int max = 0;
        if (right != null) {
            max = EvaluationUtil.getNumber(right, env).intValue();
            return new NumericalRange(min, max);
        }
        return new NumericalRange(min);
    }
    
    boolean isTrue(Environment env) {
        String msg = "Error " + getStartLocation() + ". " 
                    + "\nExpecting a boolean here."
                    + " Expression " + this + " is a range.";
        throw new NonBooleanException(msg, env);
    }

    boolean isLiteral() {
        boolean rightIsLiteral = right == null || right.isLiteral();
        return constantValue != null || (left.isLiteral() && rightIsLiteral);
    }
    
    Expression _deepClone(String name, Expression subst) {
        return new Range(left.deepClone(name, subst), right.deepClone(name, subst));
    }
}
