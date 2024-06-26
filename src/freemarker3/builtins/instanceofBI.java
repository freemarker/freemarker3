package freemarker3.builtins;

import freemarker3.core.Environment;
import freemarker3.core.nodes.generated.BuiltInExpression;
import freemarker3.core.variables.EvaluationException;
import java.util.function.Function;

/**
 * Implementation of ?instanceof built-in 
 */
public class instanceofBI extends ExpressionEvaluatingBuiltIn {
    
    @Override
    public Function<String,Boolean> get(Environment env, BuiltInExpression caller, Object object) {
        return arg -> {
            try {
                Class<?> clazz = Class.forName(arg);
                return clazz.isInstance(object);
            } catch (Exception e) {
                throw new EvaluationException(e);
            }
        };
    }
}