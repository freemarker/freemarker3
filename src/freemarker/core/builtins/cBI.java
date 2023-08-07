package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateNumberModel;

/**
 * Implementation of ?c built-in 
 */
public class cBI extends ExpressionEvaluatingBuiltIn {
    
    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            Object model) 
    {
        final Number num;
        try {
            num = ((TemplateNumberModel) model).getAsNumber();
        } catch (ClassCastException e) {
            throw new TemplateException(
                    "Expecting a number on the left side of ?c", env);
        } catch (NullPointerException e) {
            throw new InvalidReferenceException("Undefined number", env);
        }
        if (num instanceof Integer) {
            // We accelerate this fairly common case
            return new StringModel(num.toString());
        } else {
            return new StringModel((env == null ? Environment.getNewCNumberFormat() : env.getCNumberFormat()).format(num));
        }
    }
}