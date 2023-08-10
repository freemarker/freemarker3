package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.core.parser.Token;
import freemarker.core.parser.ast.Identifier;
import freemarker.template.TemplateHashModel;

/**
 * The dot operator. Used to reference items inside a
 * <code>TemplateHashModel</code>.
 */
public class DotVariable extends Expression {
/*
    public DotVariable() {}

    public DotVariable(Expression target, Token key) {
        add(target);
        add(key);
    }*/
    
    public Expression getTarget() {
    	return (Expression) get(0);
    }
    
    public String getKey() {
    	return get(2).toString();
    }

    public Object getAsTemplateModel(Environment env) {
        Object leftModel = getTarget().getAsTemplateModel(env);
        if(leftModel instanceof TemplateHashModel) {
            return ((TemplateHashModel) leftModel).get(getKey());
        }
        throw invalidTypeException(leftModel, getTarget(), env, "hash");
    }

    public Expression _deepClone(String name, Expression subst) {
        Expression clonedTarget = getTarget().deepClone(name, subst);
        Token op = (Token) get(1);
        Token key = (Token) get(2);
        Expression result = new DotVariable();
        result.add(clonedTarget);
        result.add(op);
        result.add(key);
        return result;
    	//return new DotVariable(getTarget().deepClone(name, subst), (Token) get(2));
    }

    public boolean onlyHasIdentifiers() {
        Expression target = getTarget();
        return (target instanceof Identifier) 
               || ((target instanceof DotVariable) 
               && ((DotVariable) target).onlyHasIdentifiers());
    }
}