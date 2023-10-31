package freemarker.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;

/**
 * Implementation of ?c built-in 
 */

public class sourceBI extends BuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller)
    {
        return caller.getTarget().getSource(); //REVISIT
    }
}
