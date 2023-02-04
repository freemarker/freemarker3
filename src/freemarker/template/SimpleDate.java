package freemarker.template;

import java.util.Date;

/**
 * A simple implementation of the <tt>TemplateDateModel</tt>
 * interface. Note that this class is immutable.
 * <p>This class is thread-safe.
 * 
 * @version $Id: SimpleDate.java,v 1.11 2004/03/13 13:05:09 ddekany Exp $
 * @author Attila Szegedi
 */
public class SimpleDate implements TemplateDateModel
{
    private final Date date;
    private final int type;
    
    /**
     * Creates a new date model wrapping the specified date object and
     * having DATE type.
     */
    public SimpleDate(java.sql.Date date) {
        this(date, DATE);
    }
    
    /**
     * Creates a new date model wrapping the specified time object and
     * having TIME type.
     */
    public SimpleDate(java.sql.Time time) {
        this(time, TIME);
    }
    
    /**
     * Creates a new date model wrapping the specified time object and
     * having DATETIME type.
     */
    public SimpleDate(java.sql.Timestamp datetime) {
        this(datetime, DATETIME);
    }
    
    /**
     * Creates a new date model wrapping the specified date object and
     * having the specified type.
     */
    public SimpleDate(Date date, int type) {
        if(date == null) {
            throw new IllegalArgumentException("date == null");
        }
        this.date = (Date)date.clone();
        this.type = type;
    }
    
    public Date getAsDate() {
        return (Date)date.clone();
    }

    public int getDateType() {
        return type;
    }
    
    public String toString() {
        return date.toString();
    }
}
