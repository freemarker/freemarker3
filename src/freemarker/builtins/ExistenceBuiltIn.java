package freemarker.builtins;

import java.util.List;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.nodes.generated.Expression;
import freemarker.core.nodes.generated.ParentheticalExpression;
import freemarker.core.variables.WrappedHash;
import freemarker.core.variables.WrappedMethod;
import freemarker.core.variables.InvalidReferenceException;

import static freemarker.core.variables.Wrap.*;

public abstract class ExistenceBuiltIn extends BuiltIn {
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
        public Object apply(final Object model) {
            if(model == null || model == JAVA_NULL) {
                return FirstDefined.INSTANCE;
            }
            return new WrappedMethod() {
                public Object exec(List arguments) {
                    return model;
                }
            };
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
        public Object apply(final Object model) {
            if (model == null || model == JAVA_NULL || model == NOTHING) return false;
            if (isIterable(model)) {
                return asIterator(model).hasNext();
            }
            if (model instanceof Map) {
                return !((Map<?,?>) model).isEmpty();
            }
            if (model instanceof WrappedHash) {
                return !((WrappedHash)model).isEmpty();
            }
            if (isString(model)) {
                return asString(model).length() > 0;
            }
            return true;
        }
    };

    public static class IsDefinedBuiltIn extends ExistenceBuiltIn {
        public Object apply(final Object model) {
            return model != null;
        }
    };

    private static class FirstDefined implements WrappedMethod {
        static final FirstDefined INSTANCE = new FirstDefined();
        public Object exec(List args) {
            for (Object arg : args) {
                if (arg != null && arg != JAVA_NULL) {
                    return arg;
                }
            }
            return null;
        }
    };
}