package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public abstract class ExpressionEvaluatingBuiltIn extends BuiltIn {

    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller) 
    {
        return get(env, caller, caller.getTarget().getAsTemplateModel(env));
    }
    
    public abstract TemplateModel get(Environment env, BuiltInExpression caller, 
            TemplateModel model) throws TemplateException;
}
