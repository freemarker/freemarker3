package freemarker.core.ast;

import freemarker.template.*;
import freemarker.ext.beans.BeanModel;
import freemarker.core.Environment;

/**
 * An abstract class for nodes in the parse tree 
 * that represent a FreeMarker expression.
 */
abstract public class Expression extends TemplateNode {

    abstract Object _getAsTemplateModel(Environment env) throws TemplateException;
    abstract boolean isLiteral();
    
    public String getDescription() {
    	return "the expression: "  + this;
    }

    // Used to store a constant return value for this expression. Only if it
    // is possible, of course.
    
    Object constantValue;
    
    /**
     * @return the value of the expression if it is a literal, null otherwise.
     */
    
    public final Object literalValue() {
    	return constantValue;
    }
    
    // Hook in here to set the constant value if possible.
    
    public void setLocation(Template template, int beginColumn, int beginLine, int endColumn, int endLine)
    {
        super.setLocation(template, beginColumn, beginLine, endColumn, endLine);
        if (isLiteral()) {
        	try {
        		constantValue = _getAsTemplateModel(null);
        	} catch (Exception e) {
        		constantValue = TemplateModel.INVALID_EXPRESSION; // If we can't evaluate it, it must be invalid, no?
        	}
        }
    }
    
    public final Object getAsTemplateModel(Environment env) {
        return constantValue != null ? constantValue : _getAsTemplateModel(env);
    }
    
    String getStringValue(Environment env) {
        return getStringValue(getAsTemplateModel(env), this, env);
    }
    
    static boolean isDisplayableAsString(Object tm) {
    	return tm instanceof TemplateScalarModel
    	     ||tm instanceof TemplateNumberModel
    	     || tm instanceof TemplateDateModel;
    }
    
    static public String getStringValue(Object referentModel, Expression exp, Environment env)
    {
        if (referentModel instanceof TemplateNumberModel) {
            return env.formatNumber(EvaluationUtil.getNumber((TemplateNumberModel) referentModel, exp, env));
        }
        if (referentModel instanceof TemplateDateModel) {
            TemplateDateModel dm = (TemplateDateModel) referentModel;
            return env.formatDate(EvaluationUtil.getDate(dm, exp, env), dm.getDateType());
        }
        if (referentModel instanceof TemplateScalarModel) {
            return EvaluationUtil.getString((TemplateScalarModel) referentModel, exp, env);
        }
        assertNonNull(referentModel, exp, env);
        String msg = "Error " + exp.getStartLocation()
                     +"\nExpecting a string, " 
                     + "date or number here, Expression " + exp 
                     + " is instead a " 
                     + referentModel.getClass().getName();
        throw new NonStringException(msg, env);
    }

    Expression deepClone(String name, Expression subst) {
        Expression clone = _deepClone(name, subst);
        clone.copyLocationFrom(this);
        clone.parent = this.parent;
        return clone;
    }

    abstract Expression _deepClone(String name, Expression subst);

    boolean isTrue(Environment env) {
        Object referent = getAsTemplateModel(env);
        if (referent instanceof TemplateBooleanModel) {
            return ((TemplateBooleanModel) referent).getAsBoolean();
        }
        assertNonNull(referent, this, env);
        String msg = "Error " + getStartLocation()
                     + "\nExpecting a boolean (true/false) expression here"
                     + "\nExpression " + this + " does not evaluate to true/false "
                     + "\nit is an instance of " + referent.getClass().getName();
        throw new NonBooleanException(msg, env);
    }


    public Expression getParent() {
	    return (Expression) parent;
	}
    
    
	static public boolean isEmpty(Object model) throws TemplateModelException
    {
        if (model instanceof BeanModel) {
            return ((BeanModel) model).isEmpty();
        } else if (model instanceof TemplateSequenceModel) {
            return ((TemplateSequenceModel) model).size() == 0;
        } else if (model instanceof TemplateScalarModel) {
            String s = ((TemplateScalarModel) model).getAsString();
            return (s == null || s.length() == 0);
        } else if (model instanceof TemplateCollectionModel) {
            return !((TemplateCollectionModel) model).iterator().hasNext();
        } else if (model instanceof TemplateHashModel) {
            return ((TemplateHashModel) model).isEmpty();
        } else if (model instanceof TemplateNumberModel
                || model instanceof TemplateDateModel
                || model instanceof TemplateBooleanModel) {
            return false;
        } else {
            return true;
        }
    }
}
