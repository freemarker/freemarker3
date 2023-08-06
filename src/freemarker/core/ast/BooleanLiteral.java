package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;

public class BooleanLiteral extends Expression {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }
    
    public boolean getValue() {
    	return value;
    }

    boolean isTrue(Environment env) {
        return value;
    }

    public String toString() {
        return value ? "true" : "false";
    }

    public Object _getAsTemplateModel(Environment env) {
        return value ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
    }

    public boolean isLiteral() {
        return true;
    }

    Expression _deepClone(String name, Expression subst) {
    	return new BooleanLiteral(value);
    }
}
