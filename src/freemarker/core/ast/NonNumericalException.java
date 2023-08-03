package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

/**
 * A subclass of TemplateException that 
 * indicates that the internals expected an expression
 * to evaluate to a numerical value and it didn't.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
public class NonNumericalException extends TemplateException {

    public NonNumericalException(Environment env) {
        super("expecting numerical value here", env);
    }

    public NonNumericalException(String description, Environment env) {
        super(description, env);
    }
}
