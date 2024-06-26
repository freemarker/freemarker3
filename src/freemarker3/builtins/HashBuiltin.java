package freemarker3.builtins;

import freemarker3.core.Environment;
import freemarker3.core.nodes.generated.BuiltInExpression;
import freemarker3.core.nodes.generated.TemplateNode;
import freemarker3.template.TemplateHashModel;

import static freemarker3.core.variables.Wrap.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * Implementation of ?resolve built-in 
 */

public abstract class HashBuiltin extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller, Object lhs) 
    {
        if (!(lhs instanceof TemplateHashModel) && !isMap(lhs)) {
            throw TemplateNode.invalidTypeException(lhs, 
                    caller.getTarget(), env, "hash");
        }
        return apply(unwrap(lhs));
    }
    
    public abstract Iterable apply(Object hash); 
    
    public static class Keys extends HashBuiltin {
        @Override
        public Iterable apply(Object hash) {
            if (hash instanceof Map) {
                return new ArrayList(((Map)hash).keySet());
            }
            return ((TemplateHashModel) hash).keys();
        }
    }

    public static class Values extends HashBuiltin {
        @Override
        public Iterable apply(Object hash)
        {
            if (hash instanceof TemplateHashModel) {
                return ((TemplateHashModel)hash).values();
            }
            return new ArrayList(((Map) hash).values());
        }
    }
}
