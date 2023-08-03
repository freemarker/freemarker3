
 package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.Constants;
import freemarker.template.TemplateModel;

public class NullLiteral extends Expression {

    public String toString() {
    	return "null";
    }

    TemplateModel _getAsTemplateModel(Environment env) {
        return Constants.JAVA_NULL;
    }

    boolean isLiteral() {
    	return true;
    }

    Expression _deepClone(String name, Expression subst) {
    	return new NullLiteral();
    }
}
