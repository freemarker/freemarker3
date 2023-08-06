package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;

/**
 * A reference to a top-level variable
 */
public class Identifier extends Expression {

    private String name;

    public Identifier(String name) {
        this.name = name;
    }
    
    public String getName() {
    	return name;
    }

    public Object _getAsTemplateModel(Environment env) {
        try {
            return env.getVariable(name);
        } catch (NullPointerException e) {
            if (env == null) {
                throw new TemplateException("Variables are not available "
                + "(certainly you are in a parse-time executed directive). The name of the variable "
                + "you tried to read: " + name, null);
            } else {
                throw e;
            }
        }
    }

    public String toString() {
        return name;
    }

    public boolean isLiteral() {
        return false;
    }

    public Expression _deepClone(String name, Expression subst) {
        if(this.name.equals(name)) {
        	return subst.deepClone(null, null);
        }
        return new Identifier(this.name);
    }

}
