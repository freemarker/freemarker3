package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;

import static freemarker.ext.beans.ObjectWrapper.*;

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

    private int operation;

    public ArithmeticExpression() {}

    public ArithmeticExpression(Expression left, Expression right, int operation) {
        add(left);
        add(right);
        this.operation = operation;
    }
    
    public Expression getLeft() {
    	return (Expression) get(0);
    }
    
    public Expression getRight() {
    	return (Expression) get(1);
    }
    
    public int getOperation() {
    	return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }
    
    public Object evaluate(Environment env) throws TemplateException 
    {
        Object leftModel = getLeft().evaluate(env);
        Object rightModel = getRight().evaluate(env);
        boolean leftIsNumber = isNumber(leftModel);
        boolean rightIsNumber = isNumber(rightModel);
        boolean bothNumbers = leftIsNumber && rightIsNumber;
        if (!bothNumbers) {
            String msg = "Error " + getStartLocation();
            if (!leftIsNumber) {
                msg += "\nExpression " + getLeft() + " is not numerical";
            }
            if (!rightIsNumber) {
                msg += "\nExpression " + getRight() + " is not numerical";
            }
            throw new NonNumericalException(msg, env);
        }
        Number first = EvaluationUtil.getNumber(leftModel, getLeft(), env);
        Number second = EvaluationUtil.getNumber(rightModel, getRight(), env);
        ArithmeticEngine ae = 
            env != null 
                ? env.getArithmeticEngine()
                : getTemplate().getArithmeticEngine();
        switch (operation) {
            case SUBTRACTION : 
                return ae.subtract(first, second);
            case MULTIPLICATION :
                return ae.multiply(first, second);
            case DIVISION :
                return ae.divide(first, second);
            case MODULUS :
                return ae.modulus(first, second);
            default:
                throw new TemplateException("unknown operation : " + operation, env);
        }
    }

    public Expression _deepClone(String name, Expression subst) {
    	return new ArithmeticExpression(getLeft().deepClone(name, subst), getRight().deepClone(name, subst), operation);
    }
}
