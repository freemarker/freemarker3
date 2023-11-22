package freemarker.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.variables.EvaluationException;
import freemarker.core.variables.Callable;

/**
 * Implementation of ?instanceof built-in 
 */
public class instanceofBI extends ExpressionEvaluatingBuiltIn {
    
    @Override
    public Callable get(Environment env, BuiltInExpression caller, Object object) {
        return args -> {
            if (args.length != 1) {
                throw new EvaluationException("Expecting exactly one argument here");
            }
            try {
                Class<?> clazz = Class.forName(args[0].toString());
                return clazz.isInstance(object);
            } catch (Exception e) {
                throw new EvaluationException(e);
            }
        };
    }
}