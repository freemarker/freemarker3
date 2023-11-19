package freemarker.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.variables.EvaluationException;
import freemarker.core.variables.WrappedMethod;
import java.util.List;

/**
 * Implementation of ?is_a built-in 
 */
public class instanceofBI extends ExpressionEvaluatingBuiltIn {
    
    @Override
    public Object get(Environment env, BuiltInExpression caller, Object object) {
        return new WrappedMethod() {
            public Object exec(List<Object> args) {
                if (args.size() != 1) {
                    throw new EvaluationException("Expecting exactly one argument here");
                }
                String arg = args.get(0).toString();
                try {
                    Class<?> clazz = Class.forName(arg);
                    return clazz.isInstance(object);
                } catch (Exception e) {
                    throw new EvaluationException(e);
                }
            }
        };
    }
}