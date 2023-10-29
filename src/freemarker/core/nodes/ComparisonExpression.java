package freemarker.core.nodes;

import static freemarker.core.variables.ObjectWrapper.*;
import freemarker.core.EvaluationUtil;
import freemarker.core.nodes.generated.Expression;
import freemarker.core.nodes.generated.TemplateNode;
import freemarker.core.ArithmeticEngine;
import freemarker.core.Environment;
import freemarker.core.variables.*;
import freemarker.template.TemplateException;
import java.util.Date;
import java.text.Collator;

import freemarker.core.parser.Token.TokenType;
import static freemarker.core.parser.Token.TokenType.*;


public class ComparisonExpression extends TemplateNode implements Expression {

    public Expression getLeft() {
        return (Expression) get(0);
    }

    public Expression getRight() {
        return (Expression) get(2);
    }

    public TokenType getOperation() {
        return (TokenType) get(1).getType();
    }

    public Object evaluate(Environment env) {
        return isTrue(env);
    }

    /*
    * WARNING! This algorithm is duplicated in SequenceBuiltins.modelsEqual.
    * Thus, if you update this method, then you have to update that too!
    */
    public boolean isTrue(Environment env) {
        TokenType operation = getOperation();
        Object ltm = getLeft().evaluate(env);
        Object rtm = getRight().evaluate(env);
        getLeft().assertNonNull(ltm, env);
        getRight().assertNonNull(rtm, env);
        int comp = 0;
        if (isNumber(ltm) && isNumber(rtm)) {
            Number first = asNumber(ltm);
            Number second = asNumber(rtm);
            ArithmeticEngine ae = env != null ? env.getArithmeticEngine() : getTemplate().getArithmeticEngine();
            comp = ae.compareNumbers(first, second);
        } else if (ltm instanceof WrappedDate && rtm instanceof WrappedDate) {
            WrappedDate ltdm = (WrappedDate) ltm;
            WrappedDate rtdm = (WrappedDate) rtm;
            int ltype = ltdm.getDateType();
            int rtype = rtdm.getDateType();
            if (ltype != rtype) {
                throw new TemplateException("Can not compare dates of different type. Left date is of " + WrappedDate.TYPE_NAMES.get(ltype) + " type, right date is of " + WrappedDate.TYPE_NAMES.get(rtype) + " type.", env);
            }
            if (ltype == WrappedDate.UNKNOWN) {
                throw new TemplateException("Left date is of UNKNOWN type, and can not be compared.", env);
            }
            if (rtype == WrappedDate.UNKNOWN) {
                throw new TemplateException("Right date is of UNKNOWN type, and can not be compared.", env);
            }
            Date first = EvaluationUtil.getDate(ltdm, getLeft(), env);
            Date second = EvaluationUtil.getDate(rtdm, getRight(), env);
            comp = first.compareTo(second);
        } else if (isString(ltm) && isString(rtm)) {
            if (operation != EQUALS && operation != DOUBLE_EQUALS && operation != NOT_EQUALS) {
                throw new TemplateException("Can not use operator " + operation + " on string values.", env);
            }
            String first = asString(ltm);
            String second = asString(rtm);
            Collator collator;
            if (env == null) {
                collator = Collator.getInstance(getTemplate().getLocale());
            } else {
                collator = env.getCollator();
            }
            comp = collator.compare(first, second);
        } else if (isBoolean(ltm) && isBoolean(rtm)) {
            if (operation != EQUALS && operation != NOT_EQUALS) {
                throw new TemplateException("Can not use operator " + operation + " on boolean values.", env);
            }
            boolean first = asBoolean(ltm);
            boolean second = asBoolean(rtm);
            comp = (first ? 1 : 0) - (second ? 1 : 0);
        } else if (ltm instanceof Pojo && rtm instanceof Pojo) {
            Object left = ((Pojo) ltm).getWrappedObject();
            Object right = ((Pojo) rtm).getWrappedObject();
            if (operation == EQUALS || operation == DOUBLE_EQUALS) {
                return left.equals(right);
            }
            if (operation == NOT_EQUALS) {
                return !left.equals(right);
            }
            throw new UnsupportedOperationException();
        } else {
            throw new TemplateException("The only legal comparisons are between two numbers, two strings, or two dates.\n" + "Left  hand operand is a " + ltm.getClass().getName() + "\n" + "Right hand operand is a " + rtm.getClass().getName() + "\n", env);
        }
        switch(getOperation()) {
            case EQUALS : case DOUBLE_EQUALS : 
                return comp == 0;
            case NOT_EQUALS : 
                return comp != 0;
            case LESS_THAN : 
                return comp < 0;
            case GREATER_THAN : case ESCAPED_GT : 
                return comp > 0;
            case LESS_THAN_EQUALS : 
                return comp <= 0;
            case GREATER_THAN_EQUALS : case ESCAPED_GTE : 
                return comp >= 0;
            default : 
                throw new TemplateException("unknown operation", env);
        }
    }

    public Expression _deepClone(String name, Expression subst) {
        ComparisonExpression result = new ComparisonExpression();
        result.add(getLeft().deepClone(name, subst));
        result.add(get(1));
        result.add(getRight().deepClone(name, subst));
        return result;
    }

}


