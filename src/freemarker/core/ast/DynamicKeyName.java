package freemarker.core.ast;

import java.util.ArrayList;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.ext.beans.ListModel;
import freemarker.ext.beans.StringModel;

import static freemarker.ext.beans.ObjectWrapper.*;

/**
 * A unary operator that uses the string value of an expression as a hash key.
 * It associates with the <tt>Identifier</tt> or <tt>Dot</tt> to its left.
 */
public class DynamicKeyName extends Expression {

    private Expression nameExpression;
    private Expression target;

    public DynamicKeyName() {
    }

    public DynamicKeyName(Expression target, Expression nameExpression) {
        this.target = target; 
        this.nameExpression = nameExpression;
        target.setParent(this);
        nameExpression.setParent(this);
    }
    
    public Expression getNameExpression() {
    	return nameExpression;
    }
    
    public Expression getTarget() {
    	return target;
    }

    public Object evaluate(Environment env) throws TemplateException
    {
        Object targetModel = target.evaluate(env);
        assertNonNull(targetModel, target, env);
        if (nameExpression instanceof Range) {
            return dealWithRangeKey(targetModel, (Range) nameExpression, env);
        }
        Object key = nameExpression.evaluate(env);
        if(key == null) {
            assertNonNull(key, nameExpression, env);
        }
        if (isNumber(key)) {
            int index = asNumber(key).intValue();
            return dealWithNumericalKey(targetModel, index, env);
        }
        if (isString(key)) {
            return dealWithStringKey(targetModel, asString(key), env);
        }
        throw invalidTypeException(key, nameExpression, env, "number, range, or string");
    }


    private Object dealWithNumericalKey(Object targetModel, 
                                               int index, 
                                               Environment env)
    {
        if (targetModel instanceof TemplateSequenceModel) {
            TemplateSequenceModel tsm = (TemplateSequenceModel) targetModel;
            int size = Integer.MAX_VALUE;
            try {
                size = tsm.size();
            } catch (Exception e) {}
            return index<size ? tsm.get(index) : null;
        } 
        try
        {
            String s = target.getStringValue(env);
            try {
               return new StringModel(s.substring(index, index+1));
            } catch (RuntimeException re) {
                throw new TemplateException("", re, env);
            }
        }
        catch(NonStringException e)
        {
            throw invalidTypeException(targetModel, target, env, "number, sequence, or string");
        }
    }


    private Object dealWithStringKey(Object targetModel, 
                                            String key,
                                            Environment env)
    {
        if (targetModel instanceof Map) {
            return ((Map) targetModel).get(key);
        }
        if(targetModel instanceof TemplateHashModel) {
            return((TemplateHashModel) targetModel).get(key);
        }
        throw invalidTypeException(targetModel, target, env, "hash");
    }

    private Object dealWithRangeKey(Object targetModel, 
                                           Range range, 
                                           Environment env)
    {
        int start = EvaluationUtil.getNumber(range.getLeft(), env).intValue();
        int end = 0;
        boolean hasRhs = range.hasRhs();
        if (hasRhs) {
            end = EvaluationUtil.getNumber(range.getRight(), env).intValue();
        }
        if (targetModel instanceof TemplateSequenceModel) {
            TemplateSequenceModel sequence = (TemplateSequenceModel) targetModel;
            if (!hasRhs) end = sequence.size() -1;
            if (start < 0) {
                String msg = range.getRight().getStartLocation() + "\nNegative starting index for range, is " + range;
                throw new TemplateException(msg, env);
            }
            if (end < 0) {
                String msg = range.getLeft().getStartLocation() + "\nNegative ending index for range, is " + range;
                throw new TemplateException(msg, env);
            }
            if (start >= sequence.size()) {
                String msg = range.getLeft().getStartLocation() 
                            + "\nLeft side index of range out of bounds, is " + start
                            + ", but the sequence has only " + sequence.size() + " element(s) "
                            + "(note that indices are 0 based, and ranges are inclusive).";
                throw new TemplateException(msg, env);
            }
            if (end >= sequence.size()) {
                String msg = range.getRight().getStartLocation() 
                             + "\nRight side index of range out of bounds, is " + end
                             + ", but the sequence has only " + sequence.size() + " element(s)."
                             + "(note that indices are 0 based, and ranges are inclusive).";
                throw new TemplateException(msg, env);
            }
            ArrayList<Object> list = new ArrayList<>(1+Math.abs(start-end));
            if (start>end) {
                for (int i = start; i>=end; i--) {
                    list.add(sequence.get(i));
                }
            }
            else {
                for (int i = start; i<=end; i++) {
                    list.add(sequence.get(i));
                }
            }
            return new ListModel(list);
        }
        
        try
        {
            String s = target.getStringValue(env);
            if (!hasRhs) end = s.length() -1;
            if (start < 0) {
                String msg = range.getLeft().getStartLocation() + "\nNegative starting index for range " + range + " : " + start;
                throw new TemplateException(msg, env);
            }
            if (end < 0) {
                String msg = range.getLeft().getStartLocation() + "\nNegative ending index for range " + range + " : " + end;
                throw new TemplateException(msg, env);
            }
            if (start > s.length()) {
                String msg = range.getLeft().getStartLocation() 
                            + "\nLeft side of range out of bounds, is: " + start
                            + "\nbut string " + targetModel + " has " + s.length() + " elements.";
                throw new TemplateException(msg, env);
            }
            if (end > s.length()) {
                String msg = range.getRight().getStartLocation() 
                             + "\nRight side of range out of bounds, is: " + end
                             + "\nbut string " + targetModel + " is only " + s.length() + " characters.";
                throw new TemplateException(msg, env);
            }
            try {
                return new StringModel(s.substring(start, end+1));
            } catch (RuntimeException re) {
                String msg = "Error " + getStartLocation();
                throw new TemplateException(msg, re, env);
            }
        }
        catch(NonStringException e)
        {
            throw invalidTypeException(target.evaluate(env), target, env, "number, scalar, or sequence");
        }
    }

    public Expression _deepClone(String name, Expression subst) {
    	return new DynamicKeyName(target.deepClone(name, subst), nameExpression.deepClone(name, subst));
    }
}
