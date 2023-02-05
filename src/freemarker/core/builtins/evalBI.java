package freemarker.core.builtins;

import java.io.StringReader;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.Expression;
import freemarker.core.parser.FMLexer;
import freemarker.core.parser.FMParser;
import freemarker.core.parser.ParseException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Implementation of ?eval built-in 
 */

public class evalBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public boolean isSideEffectFree() {
        return false;
    }

    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) 
    throws TemplateException {
        try {
            return eval(((TemplateScalarModel) model).getAsString(), env, caller);
        } catch (ClassCastException cce) {
            throw new TemplateModelException("Expecting string on left of ?eval built-in");

        } catch (NullPointerException npe) {
            throw new TemplateModelException(npe);
        }
    }

    TemplateModel eval(String s, Environment env, BuiltInExpression caller) 
    throws TemplateException {
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
        return exp.getAsTemplateModel(env);
    }
}