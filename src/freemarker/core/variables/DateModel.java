package freemarker.core.variables;

import java.util.Date;

/**
 * Wraps arbitrary subclass of {@link java.util.Date} into a reflective model.
 * Beside acting as a {@link WrappedDate}, you can call all Java methods
 * on these objects as well.
 */
public class DateModel extends Pojo implements WrappedDate
{
    private int type;
    
    /**
     * Creates a new model that wraps the specified date object.
     * @param date the date object to wrap into a model.
     */
    public DateModel(Date date)
    {
        super(date);
        if(date instanceof java.sql.Date) {
            type = DATE;
        }
        else if(date instanceof java.sql.Time) {
            type = TIME;
        }
        else if(date instanceof java.sql.Timestamp) {
            type = DATETIME;
        }
        else {
            type = Wrap.getDefaultDateType();
        }
    }

    public DateModel(Date date, int type) {
        this(date);
        this.type = type;
    }

    public Date getAsDate() {
        return (Date)object;
    }

    public int getDateType() {
        return type;
    }
}
