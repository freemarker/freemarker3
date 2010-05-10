package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.ast.ArithmeticEngine;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.NonNumericalException;
import freemarker.core.ast.TemplateNode;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class numberBI extends ExpressionEvaluatingBuiltIn
{
    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) throws TemplateException
    {
        if(model instanceof TemplateNumberModel) {
            return model;
        }
        final String string;
        try {
            string = ((TemplateScalarModel) model).getAsString();
        }
        catch(ClassCastException ex) {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "string or number");
        }
        ArithmeticEngine e = env == null ? caller.getTemplate().getArithmeticEngine() : env.getArithmeticEngine();
        try {
            return new SimpleNumber(e.toNumber(string));
        } catch(NumberFormatException nfe) {
                String mess = "Error: " + caller.getStartLocation()
                + "\nExpecting a number in string here, found: " + string;
                throw new NonNumericalException(mess, env);
            }
    }
}