package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;

public class ParentheticalExpression extends Expression {

    private Expression nested;

    public ParentheticalExpression(Expression nested) {
        this.nested = nested;
        nested.parent = this;
    }

    boolean isTrue(Environment env) {
        return nested.isTrue(env);
    }
    
    public Expression getNested() {
    	return nested;
    }
    
    Object _getAsTemplateModel(Environment env) {
        return nested.getAsTemplateModel(env);
    }

    boolean isLiteral() {
        return nested.isLiteral();
    }

    Expression _deepClone(String name, Expression subst) {
        return new ParentheticalExpression(nested.deepClone(name, subst));
    }
}
