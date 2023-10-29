package freemarker.core.nodes;

import static freemarker.core.evaluation.Constants.JAVA_NULL;
import static freemarker.core.evaluation.ObjectWrapper.*;
import freemarker.core.EvaluationUtil;
import freemarker.core.Scope;
import freemarker.core.evaluation.*;
import freemarker.core.Environment;
import freemarker.core.nodes.generated.Expression;
import freemarker.core.nodes.generated.RangeExpression;
import freemarker.core.nodes.generated.TemplateNode;
import freemarker.template.TemplateException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class DynamicKeyName extends TemplateNode implements Expression {

    public Expression getNameExpression() {
        return (Expression) get(2);
    }

    public Expression getTarget() {
        return (Expression) get(0);
    }

    public Object evaluate(Environment env) throws TemplateException {
        Object value = getTarget().evaluate(env);
        getTarget().assertNonNull(value, env);
        if (getNameExpression() instanceof RangeExpression) {
            return dealWithRangeKey(value, (RangeExpression) getNameExpression(), env);
        }
        Object key = getNameExpression().evaluate(env);
        if (key == null) {
            getNameExpression().assertNonNull(key, env);
        }
        if (isNumber(key)) {
            int index = asNumber(key).intValue();
            return dealWithNumericalKey(value, index, env);
        }
        if (isString(key)) {
            return dealWithStringKey(value, asString(key), env);
        }
        throw invalidTypeException(key, getNameExpression(), env, "number, range, or string");
    }

    private Object dealWithNumericalKey(Object targetModel, int index, Environment env) {
        if (targetModel instanceof WrappedSequence) {
            WrappedSequence tsm = (WrappedSequence) targetModel;
            int size = Integer.MAX_VALUE;
            try {
                size = tsm.size();
            } catch (Exception e) {
            }
            return index < size ? tsm.get(index) : JAVA_NULL;
        }
        if (isList(targetModel)) {
            try {
                return wrap(asList(targetModel).get(index));
            } catch (IndexOutOfBoundsException ae) {
                return JAVA_NULL;
            }
        }
        String s = getTarget().getStringValue(env);
        try {
            return s.substring(index, index + 1);
        } catch (RuntimeException re) {
            throw new TemplateException("", re, env);
        }
    }

    private Object dealWithStringKey(Object lhs, String key, Environment env) {
        if (lhs instanceof Map) {
            return wrap(((Map) lhs).get(key));
        }
        if (lhs instanceof WrappedHash) {
            return wrap(((WrappedHash) lhs).get(key));
        }
        if (lhs instanceof Scope) {
            return wrap(((Scope) lhs).get(key));
        }
        if (lhs instanceof Pojo) {
            return wrap(((Pojo) lhs).get(key));
        }
        throw invalidTypeException(lhs, getTarget(), env, "hash");
    }

    private Object dealWithRangeKey(Object targetModel, RangeExpression range, Environment env) {
        int start = EvaluationUtil.getNumber(range.getLeft(), env).intValue();
        int end = 0;
        boolean hasRhs = range.hasRhs();
        if (hasRhs) {
            end = EvaluationUtil.getNumber(range.getRight(), env).intValue();
        }
        if (isList(targetModel)) {
            List<?> list = asList(targetModel);
            if (!hasRhs) end = list.size() - 1;
            if (start < 0) {
                String msg = range.getRight().getLocation() + "\nNegative starting index for range, is " + range;
                throw new TemplateException(msg, env);
            }
            if (end < 0) {
                String msg = range.getLeft().getLocation() + "\nNegative ending index for range, is " + range;
                throw new TemplateException(msg, env);
            }
            if (start >= list.size()) {
                String msg = range.getLeft().getLocation() + "\nLeft side index of range out of bounds, is " + start + ", but the sequence has only " + list.size() + " element(s) " + "(note that indices are 0 based, and ranges are inclusive).";
                throw new TemplateException(msg, env);
            }
            if (end >= list.size()) {
                String msg = range.getRight().getLocation() + "\nRight side index of range out of bounds, is " + end + ", but the sequence has only " + list.size() + " element(s)." + "(note that indices are 0 based, and ranges are inclusive).";
                throw new TemplateException(msg, env);
            }
            ArrayList<Object> result = new ArrayList<>();
            if (start > end) {
                for (int i = start; i >= end; i--) {
                    result.add(list.get(i));
                }
            } else {
                for (int i = start; i <= end; i++) {
                    result.add(list.get(i));
                }
            }
            return result;
        }
        String s = getTarget().getStringValue(env);
        if (!hasRhs) end = s.length() - 1;
        if (start < 0) {
            String msg = range.getLeft().getLocation() + "\nNegative starting index for range " + range + " : " + start;
            throw new TemplateException(msg, env);
        }
        if (end < 0) {
            String msg = range.getLeft().getLocation() + "\nNegative ending index for range " + range + " : " + end;
            throw new TemplateException(msg, env);
        }
        if (start > s.length()) {
            String msg = range.getLeft().getLocation() + "\nLeft side of range out of bounds, is: " + start + "\nbut string " + targetModel + " has " + s.length() + " elements.";
            throw new TemplateException(msg, env);
        }
        if (end > s.length()) {
            String msg = range.getRight().getLocation() + "\nRight side of range out of bounds, is: " + end + "\nbut string " + targetModel + " is only " + s.length() + " characters.";
            throw new TemplateException(msg, env);
        }
        try {
            return s.substring(start, end + 1);
        } catch (RuntimeException re) {
            String msg = "Error " + getLocation();
            throw new TemplateException(msg, re, env);
        }
    }

    public Expression _deepClone(String name, Expression subst) {
        DynamicKeyName result = new DynamicKeyName();
        result.add(getTarget().deepClone(name, subst));
        result.add(get(1));
        result.add(getNameExpression().deepClone(name, subst));
        result.add(get(3));
        return result;
    }

}


