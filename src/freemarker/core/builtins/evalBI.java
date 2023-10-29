package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.nodes.generated.Expression;
import freemarker.core.parser.FMLexer;
import freemarker.core.parser.FMParser;
import freemarker.core.parser.ParseException;
import freemarker.template.TemplateException;
import freemarker.core.evaluation.EvaluationException;

import static freemarker.core.evaluation.ObjectWrapper.*;

/**
 * Implementation of ?eval built-in 
 */

public class evalBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller, Object model) 
    {
        try {
            return eval(asString(model), env, caller);
        } catch (ClassCastException cce) {
            throw new EvaluationException("Expecting string on left of ?eval built-in");

        } catch (NullPointerException npe) {
            throw new EvaluationException(npe);
        }
    }

    Object eval(String s, Environment env, BuiltInExpression caller) 
    {
        String input = "(" + s + ")";
        FMLexer token_source= new FMLexer("input", input, FMLexer.LexicalState.EXPRESSION, caller.getBeginLine(), caller.getBeginColumn());;
        FMParser parser = new FMParser(token_source);
        parser.setTemplate(caller.getTemplate());
        Expression exp = null;
        try {
            exp = parser.Expression();
        } catch (ParseException pe) {
            pe.setTemplateName(caller.getTemplate().getName());
            throw new TemplateException(pe, env);
        }
        return exp.evaluate(env);
    }
}