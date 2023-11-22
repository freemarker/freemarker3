package freemarker.builtins;

import java.util.List;

import freemarker.core.variables.scope.Scope;
import freemarker.annotations.Parameters;
import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.variables.LegacyWrappedMethod;
import freemarker.template.TemplateException;

/**
 * Implementation of ?resolve built-in 
 */

public class resolveBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller,
            Object model) 
    {
        if (!(model instanceof Scope)) {
            throw new TemplateException("Expecting scope on left of ?resolve built-in", env);
        }
        final Scope scope = (Scope) model;
        return new LegacyWrappedMethod() {
            @Parameters("key")
            public Object exec(List args) {
                return scope.resolveVariable((String) args.get(0)); 
            }
        };
    }
}
