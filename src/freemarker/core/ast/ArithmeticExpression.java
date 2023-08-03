package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;

/**
 * An operator for arithmetic operations. Note that the + operator
 * also does string concatenation for backward compatibility.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
public class ArithmeticExpression extends Expression {

    static public final int SUBTRACTION = 0;
    static public final int MULTIPLICATION = 1;
    static public final int DIVISION = 2;
    static public final int MODULUS = 3;

    private Expression left;
    private Expression right;
    private int operation;

    public ArithmeticExpression(Expression left, Expression right, int operation) {
        this.left = left;
        this.right = right;
        this.operation = operation;
        left.parent = this;
        right.parent = this;
    }
    
    public Expression getLeft() {
    	return left;
    }
    
    public Expression getRight() {
    	return right;
    }
    
    public int getOperation() {
    	return operation;
    }
    
    TemplateModel _getAsTemplateModel(Environment env) throws TemplateException 
    {
        Object leftModel = left.getAsTemplateModel(env);
        Object rightModel = right.getAsTemplateModel(env);
        boolean leftIsNumber = (leftModel instanceof TemplateNumberModel);
        boolean rightIsNumber = (rightModel instanceof TemplateNumberModel);
        boolean bothNumbers = leftIsNumber && rightIsNumber;
        if (!bothNumbers) {
            String msg = "Error " + getStartLocation();
            if (!leftIsNumber) {
                msg += "\nExpression " + left + " is not numerical";
            }
            if (!rightIsNumber) {
                msg += "\nExpression " + right + " is not numerical";
            }
            throw new NonNumericalException(msg, env);
        }
        Number first = EvaluationUtil.getNumber(leftModel, left, env);
        Number second = EvaluationUtil.getNumber(rightModel, right, env);
        ArithmeticEngine ae = 
            env != null 
                ? env.getArithmeticEngine()
                : getTemplate().getArithmeticEngine();
        switch (operation) {
            case SUBTRACTION : 
                return new SimpleNumber(ae.subtract(first, second));
            case MULTIPLICATION :
                return new SimpleNumber(ae.multiply(first, second));
            case DIVISION :
                return new SimpleNumber(ae.divide(first, second));
            case MODULUS :
                return new SimpleNumber(ae.modulus(first, second));
            default:
                throw new TemplateException("unknown operation : " + operation, env);
        }
    }

    boolean isLiteral() {
        return constantValue != null || (left.isLiteral() && right.isLiteral());
    }

    Expression _deepClone(String name, Expression subst) {
    	return new ArithmeticExpression(left.deepClone(name, subst), right.deepClone(name, subst), operation);
    }
}
