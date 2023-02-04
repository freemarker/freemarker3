package freemarker.template;

import freemarker.core.Environment;

/**
 * Template model implementation classes should throw this exception if
 * requested data cannot be retrieved.  
 *
 * @version $Id: TemplateModelException.java,v 1.14 2003/04/22 21:03:22 revusky Exp $
 */
public class TemplateModelException extends TemplateException {
    private static final long serialVersionUID = -1707011064187135336L;

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
