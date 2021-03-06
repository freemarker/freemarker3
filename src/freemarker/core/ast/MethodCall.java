/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * 22 October 1999: This class added by Holger Arendt.
 */

package freemarker.core.ast;

import java.util.List;
import java.io.Writer;

import freemarker.core.Environment;
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
    	target.parent = this;
    	this.arguments = args;
    	args.parent = this;
    }
    
    public ArgsList getArgs() {
    	return arguments;
    }
    
    public Expression getTarget() {
    	return target;
    }
    
    TemplateModel _getAsTemplateModel(Environment env) throws TemplateException
    {
        TemplateModel targetModel = target.getAsTemplateModel(env);
        if (targetModel instanceof TemplateMethodModel) {
            TemplateMethodModel targetMethod = (TemplateMethodModel)targetModel;
            List argumentStrings = arguments.getParameterSequence(targetMethod, env);
            Object result = targetMethod.exec(argumentStrings);
            return env.getObjectWrapper().wrap(result);
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
            return sw != null ? env.getObjectWrapper().wrap(sw.getBuffer().toString()) : env.getLastReturnValue();
        }
        else if (targetModel instanceof Curry.Operator) {
            return ((Curry.Operator)targetModel).curry(arguments, env);
        }
        else {
            throw invalidTypeException(targetModel, target, env, "method");
        }
    }

    TemplateModel getConstantValue() {
        return null;
    }

    boolean isLiteral() {
        return false;
    }

    Expression _deepClone(String name, Expression subst) {
    	return new MethodCall(target.deepClone(name, subst), arguments.deepClone(name, subst));
    }

}
