package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public abstract class ExpressionEvaluatingBuiltIn extends BuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller) 
    {
        return get(env, caller, caller.getTarget().evaluate(env));
    }
    
    public abstract Object get(Environment env, BuiltInExpression caller, 
            Object model);
}
