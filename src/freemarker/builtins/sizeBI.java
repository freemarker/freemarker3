package freemarker.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.nodes.generated.TemplateNode;
import freemarker.core.variables.Hash;
import freemarker.core.variables.Sequence;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * Implementation of ?c built-in 
 */
public class sizeBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller,
            Object model) 
    {
        int size = -1;
        if (model instanceof Collection) {
            size = ((Collection<?>)model).size();
        }
        else if (model instanceof Map) {
            size = ((Map<?,?>) model).size();
        }
        else if (model instanceof Sequence) {
            size = ((Sequence) model).size();
        }
        else if (model instanceof Hash) {
            size = ((Hash) model).size();
        }
        else if (model.getClass().isArray()) {
            return Array.getLength(model);
        }
        else {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "a sequence or extended hash");
        }
        return size;
    }
}