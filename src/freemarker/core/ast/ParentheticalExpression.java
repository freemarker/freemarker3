package freemarker.core.ast;

import freemarker.core.Environment;

public class ParentheticalExpression extends Expression {

    private Expression nested;

    public ParentheticalExpression(Expression nested) {
        this.nested = nested;
        nested.setParent(this);
    }

    public boolean isTrue(Environment env) {
        return nested.isTrue(env);
    }
    
    public Expression getNested() {
    	return nested;
    }
    
    public Object getAsTemplateModel(Environment env) {
        return nested.getAsTemplateModel(env);
    }

    public Expression _deepClone(String name, Expression subst) {
        return new ParentheticalExpression(nested.deepClone(name, subst));
    }
}
