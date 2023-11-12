package freemarker.builtins;

import freemarker.core.Environment;
import freemarker.core.variables.InvalidReferenceException;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.template.TemplateException;

/**
 * Implementation of ?c built-in 
 */
public class cBI extends ExpressionEvaluatingBuiltIn {
    
    @Override
    public Object get(Environment env, BuiltInExpression caller, Object model) 
    {
        Number num;
        try {
            num = (Number)model;
        } catch (ClassCastException e) {
            throw new TemplateException(
                    "Expecting a number on the left side of ?c", env);
        } catch (NullPointerException e) {
            throw new InvalidReferenceException("Undefined number", env);
        }
        if (num instanceof Integer) {
            // We accelerate this fairly common case
            return num.toString();
        } else {
            return (env == null ? Environment.getNewCNumberFormat() : env.getCNumberFormat()).format(num);
        }
    }
}