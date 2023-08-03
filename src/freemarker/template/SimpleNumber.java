package freemarker.template;

/**
 * A simple implementation of the <tt>TemplateNumberModel</tt>
 * interface. Note that this class is immutable.
 *
 * <p>This class is thread-safe.
 *
 * @author <A HREF="mailto:jon@revusky.com">Jonathan Revusky</A>
 */
public final class SimpleNumber implements TemplateNumberModel {

    /**
     * @serial the value of this <tt>SimpleNumber</tt> 
     */
    private Number value;

    public SimpleNumber(Number value) {
        this.value = value;
    }

    public SimpleNumber(byte val) {
        this.value = Byte.valueOf(val);
    }

    public SimpleNumber(short val) {
        this.value = val;
    }

    public SimpleNumber(int val) {
        this.value = val;
    }

    public SimpleNumber(long val) {
        this.value = val;
    }

    public SimpleNumber(float val) {
        this.value = val;
    }
    
    public SimpleNumber(double val) {
        this.value = val;
    }

    public Number getAsNumber() {
        return value;
    }

    public String toString() {
        return value.toString();
    }
}
