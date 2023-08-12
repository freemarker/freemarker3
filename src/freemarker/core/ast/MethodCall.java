

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

    public MethodCall(Expression target, ArgsList args) {
        add(target);
        add(args);
    }

    public ArgsList getArgs() {
        return firstChildOfType(ArgsList.class);
    }

    public Expression getTarget() {
    	return (Expression) get(0);
    }
    
    public Object evaluate(Environment env) throws TemplateException
    {
        Object value = getTarget().evaluate(env);
        if (value instanceof TemplateMethodModel) {
            TemplateMethodModel targetMethod = (TemplateMethodModel)value;
            List argumentStrings = getArgs().getParameterSequence(targetMethod, env);
            Object result = targetMethod.exec(argumentStrings);
            return ObjectWrapper.instance().wrap(result);
        }
        else if (value instanceof Macro) {
            Macro func = (Macro) value;
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
                env.render(func, getArgs(), null, null);
            } catch (IOException ioe) {
                throw new InternalError("This should be impossible.");
            } finally {
                env.setOut(prevOut);
            }
            return sw != null ? ObjectWrapper.instance().wrap(sw.getBuffer().toString()) : env.getLastReturnValue();
        }
        else {
            throw invalidTypeException(value, getTarget(), env, "method");
        }
    }

    public Expression _deepClone(String name, Expression subst) {
    	return new MethodCall(getTarget().deepClone(name, subst), getArgs().deepClone(name, subst));
    }
}
