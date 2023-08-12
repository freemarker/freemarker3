package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.ast.ArithmeticEngine;
import freemarker.core.parser.ast.BuiltInExpression;
import freemarker.core.ast.NonNumericalException;
import freemarker.core.parser.ast.TemplateNode;
import freemarker.ext.beans.ObjectWrapper;
import freemarker.template.TemplateException;

import static freemarker.ext.beans.ObjectWrapper.*;

public class numberBI extends ExpressionEvaluatingBuiltIn
{
    @Override
    public Object get(Environment env, BuiltInExpression caller,
            Object value) throws TemplateException
    {
        if(isNumber(value)) {
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
            return wrap(e.toNumber(string));
        } catch(NumberFormatException nfe) {
                String mess = "Error: " + caller.getStartLocation()
                + "\nExpecting a number in string here, found: " + string;
                throw new NonNumericalException(mess, env);
            }
    }
}