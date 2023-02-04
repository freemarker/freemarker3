package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;

/**
 * A simple implementation of the <tt>TemplateNumberModel</tt>
 * interface. Note that this class is immutable.
 * @author <A HREF="mailto:jon@revusky.com">Jonathan Revusky</A>
 */
public class NumberLiteral extends Expression implements TemplateNumberModel {

    private Number value;

    public NumberLiteral(Number value) {
        this.value = value;
    }
    
    TemplateModel _getAsTemplateModel(Environment env) {
        return new SimpleNumber(value);
    }
    
    public Number getValue() {
    	return value;
    }

    public String getStringValue(Environment env) {
        return env.formatNumber(value);
    }

    public Number getAsNumber() {
        return value;
    }
    
    String getName() {
        return "the number: '" + value + "'";
    }

    boolean isLiteral() {
        return true; 
    }

    Expression _deepClone(String name, Expression subst) {
        return new NumberLiteral(value);
    }

}
