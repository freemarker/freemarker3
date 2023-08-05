package freemarker.core.ast;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.*;
import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.core.parser.*;
import freemarker.core.parser.FMLexer.LexicalState;
import freemarker.core.parser.ast.BaseNode;
import freemarker.ext.beans.SimpleMethodModel;

/**
 * The abstract base class of both {@link NamedArgsList} and {@link PositionalArgsList}
 * @author revusky
 */

abstract public class ArgsList extends BaseNode {
	
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
	
	abstract Map<String, Object> getParameterMap(Object target, Environment env);
	
	abstract List getParameterSequence(Object target, Environment env);
	
	static final ParameterList getParameterList(Object target) {
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
            Parameters params = getAnnotatedParameters(target);
            if (params != null) {
                String paramString = params.value();
                if("".equals(paramString)) {
                    result = new ParameterList();
                }
                else try {
                    result = getParameterList(paramString);
                } catch (Exception pe) {
                    throw new TemplateException("Can't parse parameter list [" + paramString + "] on " + target, pe, Environment.getCurrentEnvironment());
                }
                parameterListCache.put(keyName, result);
                return result;
            } else {
                parameterListCache.put(keyName, NO_PARAM_LIST);
                return null;
            }
	}
	
	private static ParameterList getParameterList(String s) {
		FMLexer token_source = new FMLexer(s);
		token_source.switchTo(LexicalState.EXPRESSION);
		FMParser parser = new FMParser(token_source);
		return parser.ParameterList();
	}
	
	@SuppressWarnings("deprecation")
	private static Parameters getAnnotatedParameters(Object target) {
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
	
	public void setLocationInfoIfAbsent(BaseNode invoker) {
		if (getBeginLine() == 0) {
			this.copyLocationFrom(invoker);
			
		}
	}
	
	abstract ArgsList deepClone(String name, Expression subst);
	
	abstract void addOOParamArg(OOParamElement param) throws ParseException;
	
//	public abstract int size();
}
