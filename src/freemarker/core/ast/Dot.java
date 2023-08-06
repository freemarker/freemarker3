package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;

/**
 * The dot operator. Used to reference items inside a
 * <code>TemplateHashModel</code>.
 */
public class Dot extends Expression {
    private Expression target;
    private String key;

    public Dot(Expression target, String key) {
        this.target = target;
        target.setParent(this);
        this.key = key;
    }
    
    public Expression getTarget() {
    	return target;
    }
    
    public String getKey() {
    	return key;
    }

    public Object _getAsTemplateModel(Environment env) {
        Object leftModel = target.getAsTemplateModel(env);
        if(leftModel instanceof TemplateHashModel) {
            return ((TemplateHashModel) leftModel).get(key);
        }
        throw invalidTypeException(leftModel, target, env, "hash");
    }

    public boolean isLiteral() {
        return target.isLiteral();
    }

    public Expression _deepClone(String name, Expression subst) {
    	return new Dot(target.deepClone(name, subst), key);
    }

    public boolean onlyHasIdentifiers() {
        return (target instanceof Identifier) 
               || ((target instanceof Dot) 
               && ((Dot) target).onlyHasIdentifiers());
    }
}