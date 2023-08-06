

/*
 * 22 October 1999: This class added by Holger Arendt.
 */package freemarker.core.ast;

import java.util.List;
import java.io.Writer;

import freemarker.core.Environment;
import freemarker.ext.beans.ObjectWrapper;
import freemarker.template.*;
import java.io.IOException;
import java.io.StringWriter;


/**
 * A unary operator that calls a TemplateMethodModel or function
 * specified in FTL.  It associates with the <tt>Identifier</tt> 
 * or <tt>Dot</tt> to its left.
 */
public class MethodCall extends Expression {

    private Expression target;
    private final ArgsList arguments;
    
    public MethodCall(Expression target, ArgsList args) {
    	this.target = target;
    	target.setParent(this);
    	this.arguments = args;
    	args.setParent(this);
    }
    
    public ArgsList getArgs() {
    	return arguments;
    }
    
    public Expression getTarget() {
    	return target;
    }
    
    public Object _getAsTemplateModel(Environment env) throws TemplateException
    {
        Object targetModel = target.getAsTemplateModel(env);
        if (targetModel instanceof TemplateMethodModel) {
            TemplateMethodModel targetMethod = (TemplateMethodModel)targetModel;
            List argumentStrings = arguments.getParameterSequence(targetMethod, env);
            Object result = targetMethod.exec(argumentStrings);
            return ObjectWrapper.instance().wrap(result);
        }
        else if (targetModel instanceof Macro) {
            Macro func = (Macro) targetModel;
            StringWriter sw = null;
            env.setLastReturnValue(null);
            Writer prevOut = env.getOut();
            try {
                env.setOut(Environment.NULL_WRITER);
                if (!func.isFunction()) {
                   // I think the previous behavior was just silly.
                    // If you use a macro in a context calling for a function, it should
                    // just return the text that the macro would output.
                    sw = new StringWriter();
                    env.setOut(sw);
//                    throw new TemplateException("A macro cannot be called in an expression.", env);
                 }
                env.render(func, arguments, null, null);
            } catch (IOException ioe) {
                throw new InternalError("This should be impossible.");
            } finally {
                env.setOut(prevOut);
            }
            return sw != null ? ObjectWrapper.instance().wrap(sw.getBuffer().toString()) : env.getLastReturnValue();
        }
        else {
            throw invalidTypeException(targetModel, target, env, "method");
        }
    }

    TemplateModel getConstantValue() {
        return null;
    }

    public boolean isLiteral() {
        return false;
    }

    Expression _deepClone(String name, Expression subst) {
    	return new MethodCall(target.deepClone(name, subst), arguments.deepClone(name, subst));
    }

}
