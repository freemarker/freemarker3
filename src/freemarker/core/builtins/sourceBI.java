package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.ext.beans.StringModel;
import freemarker.template.*;

/**
 * Implementation of ?c built-in 
 */

public class sourceBI extends BuiltIn {

    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller)
    {
        return new StringModel(caller.getTarget().getSource()); //REVISIT
    }
}
