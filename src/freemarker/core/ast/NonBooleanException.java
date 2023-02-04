package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

/**
 * A subclass of TemplateException that 
 * indicates that the internals expected an expression
 * to evaluate to a boolean value and it didn't.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
public class NonBooleanException extends TemplateException {
    private static final long serialVersionUID = -4156311663232920403L;

    public NonBooleanException(Environment env) {
        super("expecting boolean value here", env);
    }

    public NonBooleanException(String description, Environment env) {
        super(description, env);
    }
}
