package freemarker3.builtins;

import freemarker3.core.Environment;
import freemarker3.core.ArithmeticEngine;
import freemarker3.core.nodes.generated.BuiltInExpression;
import freemarker3.core.nodes.generated.TemplateNode;
import freemarker3.template.TemplateException;

import static freemarker3.core.variables.Wrap.*;

public class numberBI extends ExpressionEvaluatingBuiltIn
{
    @Override
    public Object get(Environment env, BuiltInExpression caller, Object value) {
        if(value instanceof Number) {
            return value;
        }
        final String string;
        try {
            string = asString(value);
        }
        catch(ClassCastException ex) {
            throw TemplateNode.invalidTypeException(value, caller.getTarget(), env, "string or number");
        }
        ArithmeticEngine e = env == null ? caller.getTemplate().getArithmeticEngine() : env.getArithmeticEngine();
        try {
            //return wrap(e.toNumber(string));
            return e.toNumber(string);
        } catch(NumberFormatException nfe) {
                String mess = "Error: " + caller.getLocation()
                + "\nExpecting a number in string here, found: " + string;
                throw new TemplateException(mess, env);
            }
    }
}