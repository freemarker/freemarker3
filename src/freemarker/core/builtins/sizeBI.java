package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.nodes.generated.TemplateNode;
import freemarker.template.TemplateHashModel;
import freemarker.template.WrappedSequence;
import static freemarker.ext.beans.ObjectWrapper.*;

/**
 * Implementation of ?c built-in 
 */

public class sizeBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller,
            Object model) 
    {
        int size = -1;
        if (model instanceof WrappedSequence) {
            size = ((WrappedSequence) model).size();
        }
        else if (isList(model)) {
            size=asList(model).size();
        }
        else if (model instanceof TemplateHashModel) {
            size = ((TemplateHashModel) model).size();
        }
        else {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "a sequence or extended hash");
        }
        return size;
    }
}