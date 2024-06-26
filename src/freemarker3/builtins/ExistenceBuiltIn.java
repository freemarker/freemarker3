package freemarker3.builtins;

import java.util.Map;
//import java.util.function.Supplier;
import java.util.function.Function;

import freemarker3.core.Environment;
import freemarker3.core.nodes.generated.BuiltInExpression;
import freemarker3.core.nodes.generated.Expression;
import freemarker3.core.nodes.generated.ParentheticalExpression;
import freemarker3.core.variables.VarArgsFunction;
import freemarker3.core.variables.InvalidReferenceException;
import freemarker3.template.TemplateHashModel;

import static freemarker3.core.variables.Wrap.*;

public abstract class ExistenceBuiltIn implements BuiltIn {
    public Object get(Environment env, BuiltInExpression caller) 
    {
        final Expression target = caller.getTarget();
        try {
            return apply(target.evaluate(env));
        }
        catch(InvalidReferenceException e) {
            if(!(target instanceof ParentheticalExpression)) {
                throw e;
            }
            return apply(null);
        }
    }

    public abstract Object apply(Object obj);

    public static final class DefaultBuiltIn extends ExistenceBuiltIn {
        public Object apply(final Object value) {
            if(value == null || value == JAVA_NULL) {
                return FirstDefined.INSTANCE;
            }
            return (Function<Object,Object>) arg->value;
        }
    };

    public static class IfExistsBuiltIn extends ExistenceBuiltIn {
        public Object apply(final Object model) {
            return model == null || model == JAVA_NULL ? NOTHING : model;
        }
    };

    public static class ExistsBuiltIn extends ExistenceBuiltIn {
        public Object apply(final Object model) {
            return model != null && model != JAVA_NULL;
        }
    };
        
    public static class HasContentBuiltIn extends ExistenceBuiltIn {
        public Object apply(final Object value) {
            if (value == null || value == JAVA_NULL || value == NOTHING) return false;
            if (isIterable(value)) {
                return asIterator(value).hasNext();
            }
            if (value instanceof Map) {
                return !((Map<?,?>) value).isEmpty();
            }
            if (value instanceof TemplateHashModel) {
                return !((TemplateHashModel)value).isEmpty();
            }
            return value.toString().length() > 0;
        }
    };

    public static class IsDefinedBuiltIn extends ExistenceBuiltIn {
        public Object apply(final Object model) {
            return model != null;
        }
    };

    private static class FirstDefined implements VarArgsFunction {
        static final FirstDefined INSTANCE = new FirstDefined();
        public Object apply(Object... args) {
            for (Object arg : args) {
                if (arg != null && arg != JAVA_NULL) {
                    return arg;
                }
            }
            return null;
        }
    };
}