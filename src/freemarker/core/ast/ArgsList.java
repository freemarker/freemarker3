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

package freemarker.core.ast;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.StringReader;
import java.lang.reflect.*;
import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.core.parser.*;
import freemarker.ext.beans.SimpleMethodModel;

/**
 * The abstract base class of both {@link NamedArgsList} and {@link PositionalArgsList}
 * @author revusky
 */

abstract public class ArgsList extends TemplateNode {
	
	/**
	 * Cache the retrieved annotation information, since it seems like 
	 * this operation could be a bit expensive.
	 */
	private static Map<String, ParameterList> parameterListCache = new ConcurrentHashMap<String, ParameterList>();
   // Placeholder for null - ConcurrentHashMap doesn't tolerate null values
	private static final ParameterList NO_PARAM_LIST = new ParameterList();
        
	/**
	 * Given a target TemplateModel (this will be either a TemplateTranformModel or TemplateMethodModel)
	 * it returns a key-value map of the arguments to be passed to the target.
	 * A TemplateException will be thrown if the target's parameters do not match
	 * this ArgList in some way.
	 */
	
	abstract Map<String, TemplateModel> getParameterMap(TemplateModel target, Environment env)
	throws TemplateException;
	
	abstract List getParameterSequence(TemplateModel target, Environment env) throws TemplateException;
	
	static final ParameterList getParameterList(TemplateModel target) throws TemplateException {
            String keyName = target.getClass().getName();
            if (target instanceof SimpleMethodModel) {
                keyName = target.toString();
            }
            ParameterList result = parameterListCache.get(keyName);
            if(result == NO_PARAM_LIST) {
                return null;
            }
            if(result != null) {
                return result;
            }
            if(target instanceof Curry.Curried) {
                return ((Curry.Curried)target).getParameterList();
            }
            Parameters params = getAnnotatedParameters(target);
            if (params != null) {
                String paramString = params.value();
                if("".equals(paramString)) {
                    result = new ParameterList();
                }
                else {
                    try {
                        result = getParameterList(paramString);
                    } catch (Exception pe) {
                        throw new TemplateException("Can't parse parameter list [" + paramString + "] on " + target, pe, Environment.getCurrentEnvironment());
                    }
                }
                parameterListCache.put(keyName, result);
                return result;
            } else {
                parameterListCache.put(keyName, NO_PARAM_LIST);
                return null;
            }
	}
	
	private static ParameterList getParameterList(String s) throws ParseException {
//		SimpleCharStream scs = new SimpleCharStream(new StringReader(s), 1, 1, 16* s.length());
//		FMLexer token_source = new FMLexer(scs);
		FMLexer token_source = new FMLexer(new StringReader(s));
		token_source.SwitchTo(FMConstants.EXPRESSION);
		FMParser parser = new FMParser(token_source);
		return parser.ParameterList();
	}
	
	@SuppressWarnings("deprecation")
	private static Parameters getAnnotatedParameters(TemplateModel target) {
		Parameters params = null;
		Method keyMethod = null;
		if (target instanceof TemplateTransformModel) {
			try {
				keyMethod = target.getClass().getMethod("getWriter", new Class[] {java.io.Writer.class, java.util.Map.class}); 
			} catch (Exception e) {
				// This should be impossible, since
				// if the target is a TemplateTransformModel, it has this method, no?
				throw new InternalError(e.getMessage());
			}
		}
        else if (target instanceof SimpleMethodModel) {
            params = ((SimpleMethodModel) target).getParametersAnnotation();
        }
		else if (target instanceof TemplateMethodModel) {
			try {
				keyMethod = target.getClass().getMethod("exec", new Class[] {List.class});
			} catch (Exception e) {
				// Again, this condition should be impossible. If something is
				// a TemplateMethodModel, it must implement this exec method.
				throw new InternalError(e.getMessage());
			}
		}
		if (keyMethod != null) {
			params = keyMethod.getAnnotation(Parameters.class);
		}
		if (params == null) {
			// Check if the class is annotated now
			params = target.getClass().getAnnotation(Parameters.class);
		}
		return params;
	}
	
	public void setLocationInfoIfAbsent(TemplateNode invoker) {
		if (getBeginLine() == 0) {
			this.copyLocationFrom(invoker);
			
		}
	}
	
	abstract ArgsList deepClone(String name, Expression subst);
	
	abstract void addOOParamArg(OOParamElement param) throws ParseException;
	
	abstract int size();
}
