package freemarker.builtins;

import java.util.List;

import freemarker.core.variables.scope.Scope;
import freemarker.annotations.Parameters;
import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.variables.Callable;
//import freemarker.core.variables.WrappedMethod;
import freemarker.template.TemplateException;

/**
 * Implementation of ?resolve built-in 
 */

public class resolveBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public Callable get(Environment env, BuiltInExpression caller, Object lhs) 
    {
        if (!(lhs instanceof Scope)) {
            throw new TemplateException("Expecting scope on left of ?resolve built-in", env);
        }
        Scope scope = (Scope) lhs;
        return args->scope.resolveVariable((String)args[0]);
    }
}
