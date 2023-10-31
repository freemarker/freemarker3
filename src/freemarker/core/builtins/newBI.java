package freemarker.core.builtins;

import java.util.List;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.variables.*;
import freemarker.template.TemplateException;

import static freemarker.core.variables.Wrap.*;

/**
 * Implementation of ?new built-in 
 */

public class newBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller, Object model) {
        try {
            return new ConstructorFunction(asString(model), env);
        } catch (ClassCastException cce) {
            throw new EvaluationException("Expecting string on left of ?new built-in");

        } catch (NullPointerException npe) {
            throw new InvalidReferenceException("undefined string on left of ?new built-in", env);
        }
    }

    static class ConstructorFunction implements WrappedMethod {

        private final Class<?> cl;
        private final Environment env;

        public ConstructorFunction(String classname, Environment env) {
            this.env = env;
            try {
                cl = Class.forName(classname);
                if (!WrappedVariable.class.isAssignableFrom(cl)) {
                    throw new TemplateException("Class " + cl.getName() + " does not implement freemarker.template.WrappedVariable", env);
                }
                if (Pojo.class.isAssignableFrom(cl)) {
                    throw new TemplateException("Bean Models cannot be instantiated using the ?new built-in", env);
                }
            } 
            catch (ClassNotFoundException cnfe) {
                throw new TemplateException(cnfe, env);
            }
        }

        public Object exec(List arguments) {
            return Invoke.newInstance(cl, arguments);
        }
    }
}
