package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.parser.ast.BuiltInExpression;

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
