package freemarker3.builtins;

import freemarker3.core.Environment;
import freemarker3.core.nodes.generated.BuiltInExpression;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public abstract class ExpressionEvaluatingBuiltIn implements BuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller) 
    {
        return get(env, caller, caller.getTarget().evaluate(env));
    }
    
    public abstract Object get(Environment env, BuiltInExpression caller, Object lhs);
}
