/*
 * Copyright (c) 2006 The Visigoth Software Society. All rights
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

package freemarker.core.ast;

import java.util.*;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.template.utility.UndeclaredThrowableException;

public class PositionalArgsList extends ArgsList {
	
	List<Expression> args = new ArrayList<Expression>();
	
	void setParent(Expression parent) {
		for (Expression exp : args) exp.parent = parent;
	}
	
	public List<Expression> getArgs() {
		return args;
	}
	
	public void addArg(Expression exp) {
		args.add(exp);
	}
	
	Map<String, TemplateModel> getParameterMap(TemplateModel target, Environment env)
    throws TemplateException {
    	Map<String, TemplateModel> result = new HashMap<String, TemplateModel>();
    	ParameterList annotatedParameterList = getParameterList(target);
    	if (annotatedParameterList == null) {
    		if (!args.isEmpty()) {
    			throw new TemplateModelException("Cannot pass positional arguments to this TemplateTransformModel");
    		}
    	}
    	else {
    		result = annotatedParameterList.getParameterMap(this, env);
    	}
    	return result;
    }
	
	List getParameterSequence(TemplateModel target, Environment env) throws TemplateException {
		ParameterList annotatedParameterList = getParameterList(target);
		if (annotatedParameterList == null) {
            return target instanceof TemplateMethodModelEx ? 
                    (target instanceof LazilyEvaluatableArguments ? 
                           getLazyModelList(env) :
                           getModelList(env)) :
                    (target instanceof LazilyEvaluatableArguments ? 
                            getLazyValueList(env) :
                            getValueList(env));
		}
		// TODO: Attila's lazy evaluation machinery here as well, I guess FWIW...
		List<TemplateModel> result = annotatedParameterList.getParameterSequence(this, env);
		if ((target instanceof TemplateMethodModel) && !(target instanceof TemplateMethodModelEx)) {
			List<String> strings = new ArrayList<String>();
			for (TemplateModel value : result) {
				try {
					strings.add(((TemplateScalarModel) value).getAsString());
				} catch (ClassCastException cce) {
					String msg = "Error at: " + getStartLocation() + "\nThis method only takes strings as arguments.";
					throw new TemplateException(msg, env);
				}
			}
			return strings;
		}
		return result;
	}
	
	public int size() {
		return args == null ? 0 : args.size();
	}
	
	public String getStartLocation() {
		if (args != null && args.size() >0) {
			return args.get(0).getStartLocation();
		}
		else {
			return ""; // REVISIT 
		}
	}
	
	public TemplateModel getValueAt(int i, Environment env) throws TemplateException {
		Expression exp = args.get(i);
		TemplateModel value = exp.getAsTemplateModel(env);
		TemplateNode.assertIsDefined(value, exp, env);
		return value;
	}
	
	
    private static abstract class ExpressionTransformator {
        abstract Object transform(Expression exp, Environment env) 
        throws TemplateException;
    }
    
    private static class ToValueTransformator extends ExpressionTransformator {
        static final ExpressionTransformator INSTANCE = new ToValueTransformator(); 

        Object transform(Expression exp, Environment env) 
        throws TemplateException {
            return exp.getStringValue(env);
        }
    }
    
    private static class ToModelTransformator extends ExpressionTransformator {
        static final ExpressionTransformator INSTANCE = new ToModelTransformator(); 

        Object transform(Expression exp, Environment env) 
        throws TemplateException {
            return exp.getAsTemplateModel(env);
        }
    }
    
    private List getList(Environment env, ExpressionTransformator transformator)
    throws TemplateException {
        int size = args.size();
        switch(size) {
            case 0: {
                return Collections.EMPTY_LIST;
            }
            case 1: {
            	return Collections.singletonList(transformator.transform(args.get(0), env));
            }
            default: {
                List result = new ArrayList(args.size());
                for (ListIterator iterator = args.listIterator(); iterator.hasNext();) {
                    result.add(transformator.transform((Expression)iterator.next(), env));
                }
                return result;
            }
        }
    }

    /**
     * For the benefit of method calls, return the list of arguments as a list
     * of values.
     */
    List getValueList(Environment env) throws TemplateException {
        return getList(env, ToValueTransformator.INSTANCE);
    }

    /**
     * For the benefit of extended method calls, return the list of arguments as
     * a list of template models.
     */
    List getModelList(Environment env) throws TemplateException {
        return getList(env, ToModelTransformator.INSTANCE);
    }

    /**
     * For the benefit of lazily evaluatable method calls, return the list of 
     * arguments as a list of lazily evaluated values.
     */
    List getLazyValueList(Environment env) {
        return new LazyEvaluationList(ToValueTransformator.INSTANCE, env);
    }

    /**
     * For the benefit of lazily evaluatable extended method calls, return the 
     * list of arguments as a list of lazily evaluated models.
     */
    List getLazyModelList(Environment env) {
        return new LazyEvaluationList(ToModelTransformator.INSTANCE, env);
    }

    private class LazyEvaluationList extends AbstractList {
        private final ExpressionTransformator transformator;
        private final Environment env;
        private final Object[] resolvedValues;
        private final boolean[] resolved;
        
        LazyEvaluationList(ExpressionTransformator transformator, Environment env) {
            this.transformator = transformator;
            this.env = env;
            int size = args.size();
            resolvedValues = new Object[size];
            resolved = new boolean[size];
            
        }
        
        public int size() {
            return resolvedValues.length;
        }
        
        public Object get(int index) {
            if(resolved[index]) {
                return resolvedValues[index];
            }
            Expression exp = args.get(index);
            try {
                Object obj = transformator.transform(exp, env); 
                resolved[index] = true;
                return resolvedValues[index] = obj;
            }
            catch(TemplateException e) {
                throw new UndeclaredThrowableException(e);
            }
        }
    }
    
    
    ArgsList deepClone(String name, Expression subst) {
    	PositionalArgsList result = new PositionalArgsList();
    	for (Expression arg : args) {
    		result.addArg(arg.deepClone(name, subst));
    	}
    	return result;
    }
}
