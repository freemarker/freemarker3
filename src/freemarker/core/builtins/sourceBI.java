package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.ext.beans.StringModel;

/**
 * Implementation of ?c built-in 
 */

public class sourceBI extends BuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller)
    {
        return new StringModel(caller.getTarget().getSource()); //REVISIT
    }
}
