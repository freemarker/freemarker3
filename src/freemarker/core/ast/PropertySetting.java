package freemarker.core.ast;

import freemarker.template.*;
import freemarker.core.Environment;

import static freemarker.ext.beans.ObjectWrapper.*;


/**
 * An instruction that sets a property of the template rendering
 * environment.
 */
public class PropertySetting extends TemplateElement {

    private String key;
    private Expression value;

    public PropertySetting(String key, Expression value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() {
    	return key;
    }
    
    public Expression getValue() {
    	return value;
    }

    public void execute(Environment env) {
        Object mval = value.evaluate(env);
        String strval;
        if (isString(mval)) {
            strval = asString(mval);
        } else if (isBoolean(mval)) {
            strval = asBoolean(mval) ? "true" : "false";
        } else if (isNumber(mval)) {
            strval = asNumber(mval).toString();
        } else {
            strval = value.getStringValue(env);
        }
        env.setSetting(key, strval);
    }

    public String getDescription() {
        return "setting " + key + " set to " + "\"" + value + "\" "
	    + "[" + getStartLocation() + "]";
    }
}
