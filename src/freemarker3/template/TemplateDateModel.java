package freemarker3.template;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import freemarker3.core.variables.WrappedVariable;

/**
 * Date values in a template data model should implement this interface.
 * Contrary to Java, FreeMarker actually distinguishes values that represent
 * only a time, only a date, or a combined date and time. All three are
 * represented using this single interface, however there's a method that
 *
 * @author Attila Szegedi
 *
 * @version $Id: WrappedDate.java,v 1.10 2004/03/13 13:05:09 ddekany Exp $
 */
public interface TemplateDateModel extends WrappedVariable {
    /**
     * It is not known whether the date object represents a time-only,
     * a date-only, or a datetime value.
     */
    public static final int UNKNOWN = 0;

    /**
     * The date model represents a time-only value.
     */
    public static final int TIME = 1;

    /**
     * The date model represents a date-only value.
     */
    public static final int DATE = 2;

    /**
     * The date model represents a datetime value.
     */
    public static final int DATETIME = 3;
    
    public static final List TYPE_NAMES =
        Collections.unmodifiableList(
            Arrays.asList(
                new String[] {
                    "UNKNOWN", "TIME", "DATE", "DATETIME"
                }));
    /**
     * Returns the date value. The return value must not be null.
     * @return the {@link Date} instance associated with this date wrapper.
     */
    public Date getAsDate();

    /**
     * Returns the type of the date. It can be any of <tt>TIME</tt>, 
     * <tt>DATE</tt>, or <tt>DATETIME</tt>.
     */
    public int getDateType();
}
