package freemarker.template;

import java.io.Serializable;

/**
 * A simple implementation of the <tt>TemplateScalarModel</tt>
 * interface, using a <tt>String</tt>.
 * As of version 2.0 this object is immutable.
 *
 * <p>This class is thread-safe.
 *
 * @version $Id: SimpleScalar.java,v 1.38 2004/09/10 20:50:45 ddekany Exp $
 * @see SimpleSequence
 * @see SimpleHash
 */
public final class SimpleScalar 
implements TemplateScalarModel, Serializable {
    private static final long serialVersionUID = -5354606483655405575L;
    /**
     * @serial the value of this <tt>SimpleScalar</tt> if it wraps a
     * <tt>String</tt>.
     */
    private String value;

    /**
     * Constructs a <tt>SimpleScalar</tt> containing a string value.
     * @param value the string value.
     */
    public SimpleScalar(String value) {
        this.value = value;
    }

    public String getAsString() {
        return (value == null) ? "" : value;
    }

    public String toString() {
        return value;
    }
}
