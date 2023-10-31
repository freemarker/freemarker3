package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.variables.EvaluationException;
import freemarker.core.variables.WrappedVariable;
import freemarker.core.variables.Pojo;

/**
 * Implementation of ?pojo built-in 
 */
public class pojoBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller, Object model) 
    {
        if (model instanceof WrappedVariable) {
            String message = "Cannot wrap this type, " + model.getClass() + " as a Pojo.";
            throw new EvaluationException(message);
        }
        if (model instanceof Pojo) {
            return model;
        }
        return new Pojo(model);
    }
}