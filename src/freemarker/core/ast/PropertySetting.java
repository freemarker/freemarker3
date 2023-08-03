package freemarker.core.ast;

import freemarker.template.*;
import freemarker.core.Environment;


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
        Object mval = value.getAsTemplateModel(env);
        String strval;
        if (mval instanceof TemplateScalarModel) {
            strval = ((TemplateScalarModel) mval).getAsString();
        } else if (mval instanceof TemplateBooleanModel) {
            strval = ((TemplateBooleanModel) mval).getAsBoolean() ? "true" : "false";
        } else if (mval instanceof TemplateNumberModel) {
            strval = ((TemplateNumberModel) mval).getAsNumber().toString();
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
