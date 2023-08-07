package freemarker.template;

import freemarker.core.Environment;

/**
 * Template model implementation classes should throw this exception if
 * requested data cannot be retrieved.  
 */
public class TemplateModelException extends TemplateException {

    /**
     * Constructs a <tt>TemplateModelException</tt> with no
     * specified detail message.
     */
    public TemplateModelException() {
        this(null, null);
    }

    /**
     * Constructs a <tt>TemplateModelException</tt> with the
     * specified detail message.
     *
     * @param description the detail message.
     */
    public TemplateModelException(String description) {
        this(description, null);
    }

    /**
     * Constructs a <tt>TemplateModelException</tt> with the given underlying
     * Exception, but no detail message.
     *
     * @param cause the underlying <code>Exception</code> that caused this
     * exception to be raised
     */
    public TemplateModelException(Exception cause) {
        this(null, cause);
    }

    /**
     * Constructs a TemplateModelException with both a description of the error
     * that occurred and the underlying Exception that caused this exception
     * to be raised.
     *
     * @param description the description of the error that occurred
     * @param cause the underlying <code>Exception</code> that caused this
     * exception to be raised
     */
    public TemplateModelException(String description, Exception cause) {
        super( description, cause, Environment.getCurrentEnvironment() );
    }
}
