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

    static TemplateBooleanModel getTemplateModel(boolean b) {
        return b? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
    }

    boolean isTrue(Environment env) {
        return value;
    }

    public String toString() {
        return value ? "true" : "false";
    }

    TemplateModel _getAsTemplateModel(Environment env) {
        return value ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
    }

    boolean isLiteral() {
        return true;
    }

    Expression _deepClone(String name, Expression subst) {
    	return new BooleanLiteral(value);
    }
}
