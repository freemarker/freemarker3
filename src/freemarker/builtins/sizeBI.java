package freemarker.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.nodes.generated.TemplateNode;
import freemarker.core.variables.WrappedHash;
import freemarker.core.variables.WrappedSequence;
import freemarker.core.variables.Pojo;

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
        else if (model instanceof WrappedSequence) {
            size = ((WrappedSequence) model).size();
        }
        else if (model instanceof WrappedHash) {
            size = ((WrappedHash) model).size();
        }
        else if (model instanceof Pojo) {
            size = ((Pojo)model).size();
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