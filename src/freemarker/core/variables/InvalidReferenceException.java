package freemarker.core.variables;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

/**
 * A subclass of TemplateException that says there
 * is no value associated with a given expression.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
public class InvalidReferenceException extends TemplateException {

    public InvalidReferenceException(Environment env) {
        super("invalid reference", env);
    }

    public InvalidReferenceException(String description, Environment env) {
        super(description, env);
    }
}
