package freemarker.builtins;

import java.util.function.Function;

import freemarker.core.variables.scope.Scope;
import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
//import freemarker.core.variables.VarArgsFunction;
import freemarker.template.TemplateException;

/**
 * Implementation of ?resolve built-in 
 */

public class resolveBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public Function<String,Object> get(Environment env, BuiltInExpression caller, Object lhs) 
    {
        if (!(lhs instanceof Scope)) {
            throw new TemplateException("Expecting scope on left of ?resolve built-in", env);
        }
        Scope scope = (Scope) lhs;
        return arg->scope.resolveVariable(arg);
    }
}
