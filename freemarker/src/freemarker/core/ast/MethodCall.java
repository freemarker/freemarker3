/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
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


/**
 * A unary operator that calls a TemplateMethodModel or function
 * specified in FTL.  It associates with the <tt>Identifier</tt> 
 * or <tt>Dot</tt> to its left.
 */
public class MethodCall extends Expression {

    public final Expression target;
    private final ArgsList arguments;
    
    public MethodCall(Expression target, ArgsList args) {
    	this.target = target;
    	this.arguments = args;
    }
    
    public ArgsList getArgs() {
    	return arguments;
    }
    
    TemplateModel _getAsTemplateModel(Environment env) throws TemplateException
    {
        TemplateModel targetModel = target.getAsTemplateModel(env);
        if (targetModel instanceof TemplateMethodModel) {
//        	PositionalArgsList args = (PositionalArgsList) arguments;
            TemplateMethodModel targetMethod = (TemplateMethodModel)targetModel;
            List argumentStrings = arguments.getParameterSequence(targetMethod, env);
            Object result = targetMethod.exec(argumentStrings);
            return env.getObjectWrapper().wrap(result);
        }
        else if (targetModel instanceof Macro) {
            Macro func = (Macro) targetModel;
            env.setLastReturnValue(null);
            if (!func.isFunction()) {
                throw new TemplateException("A macro cannot be called in an expression.", env);
            }
            Writer prevOut = env.getOut();
            try {
                env.setOut(Environment.NULL_WRITER);
                env.render(func, arguments, null, null, null);
            } catch (IOException ioe) {
                throw new InternalError("This should be impossible.");
            } finally {
                env.setOut(prevOut);
            }
            return env.getLastReturnValue();
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

    public boolean isLiteral() {
        return false;
    }

    Expression _deepClone(String name, Expression subst) {
    	//  TODO implement deepClone() in an ArgsList
    	return new MethodCall(target.deepClone(name, subst), arguments);
//        return new MethodCall(target.deepClone(name, subst), (ArgsList)arguments.deepClone(name, subst));
    }

}
