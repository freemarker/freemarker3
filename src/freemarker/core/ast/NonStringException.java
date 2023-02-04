package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

/**
 * A subclass of TemplateException that 
 * indicates that the internals expected an expression
 * to evaluate to a string or numeric value and it didn't.
 * @author Attila Szegedi
 */
public class NonStringException extends TemplateException {
    private static final long serialVersionUID = 102300358326821897L;

    public NonStringException(Environment env) {
        super("expecting string or numerical value here", env);
    }

    public NonStringException(String description, Environment env) {
        super(description, env);
    }
}
