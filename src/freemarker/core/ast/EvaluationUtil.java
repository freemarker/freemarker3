package freemarker.core.ast;

import java.util.Date;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;
import static freemarker.template.Constants.JAVA_NULL;;
import static freemarker.ext.beans.ObjectWrapper.*;

/**
 * @version 1.0
 * @author Attila Szegedi
 */
public class EvaluationUtil
{
    private EvaluationUtil() {}
    
    static public Number getNumber(Expression expr, Environment env)
    {
        Object value = expr.evaluate(env);
        return getNumber(value, expr, env);
    }

    static public Number getNumber(Object model, Expression expr, Environment env)
    {
        if(isNumber(model)) {
            return asNumber(model);
        }
        else if(model == null) {
            throw new InvalidReferenceException(expr + " is undefined.", env);
        }
        else if(model == JAVA_NULL) {
            throw new InvalidReferenceException(expr + " is null.", env);
        }
        else {
            throw new NonNumericalException(expr + " is not a number, it is " + model.getClass().getName(), env);
        }
    }

    static public Date getDate(TemplateDateModel model, Expression expr, Environment env)
    {
        Date value = model.getAsDate();
        if(value == null) {
            throw new TemplateException(expr + " evaluated to null date.", env);
        }
        return value;
    }
}
