package freemarker3.builtins;

import freemarker3.core.Environment;
import freemarker3.core.nodes.generated.BuiltInExpression;
import freemarker3.core.nodes.generated.TemplateNode;
import freemarker3.template.TemplateHashModel;
import freemarker3.template.TemplateSequenceModel;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * Implementation of ?c built-in 
 */
public class sizeBI extends ExpressionEvaluatingBuiltIn {

    @Override
    @SuppressWarnings("rawtypes")
    public Object get(Environment env, BuiltInExpression caller, Object value) {
        if (value instanceof Collection) {
            return ((Collection)value).size();
        }
        else if (value instanceof Map) {
            return ((Map) value).size();
        }
        else if (value instanceof TemplateSequenceModel) {
            return ((TemplateSequenceModel) value).size();
        }
        else if (value instanceof TemplateHashModel) {
            return ((TemplateHashModel) value).size();
        }
        else if (value.getClass().isArray()) {
            return Array.getLength(value);
        }
        else {
            throw TemplateNode.invalidTypeException(value, caller.getTarget(), env, "a sequence or extended hash");
        }
    }
}