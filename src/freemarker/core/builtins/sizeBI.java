package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.parser.ast.BaseNode;
import freemarker.template.*;

/**
 * Implementation of ?c built-in 
 */

public class sizeBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller,
            Object model) 
    {
        int size = -1;
        if (model instanceof TemplateSequenceModel) {
            size = ((TemplateSequenceModel) model).size();
        }
        else if (model instanceof TemplateHashModelEx) {
            size = ((TemplateHashModelEx) model).size();
        }
        else {
            throw BaseNode.invalidTypeException(model, caller.getTarget(), env, "a sequence or extended hash");
        }
        return new SimpleNumber(Integer.valueOf(size));
    }
}