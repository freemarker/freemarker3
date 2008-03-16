package freemarker.core.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.core.Scope;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Represents the specification of the parameters to a macro.
 * Note that we deal with default values and the optional catch-al
 * parameter here (varags...)
 */

// TODO: Improve the error detail messages maybe

public class ParameterList extends TemplateNode {
	
	List<String> params = new ArrayList<String>();
	private Map<String, Expression> defaults;
	private String catchall;
    private boolean curryGenerated;
	protected TemplateElement parent;
	
	public void addParam(String paramName) {
		params.add(paramName);
	}
	
	public List<String> getParamNames() {
		List<String> result = new ArrayList<String>(params);
		if (catchall != null) result.add(catchall);
		return result;
	}

    
    boolean containsParam(String name) {
        return params.contains(name);
    }
	
	public void addParam(String paramName, Expression defaultExp) {
		if (defaults == null) defaults = new HashMap<String, Expression>();
		defaults.put(paramName, defaultExp);
		addParam(paramName);
	}
	
	public void setCatchAll(String varname) {
		this.catchall = varname;
	}
	
	public String getCatchAll() {
		return catchall;
	}
	
    void setCurryGenerated(boolean curryGenerated) {
        this.curryGenerated = curryGenerated;
    }
        
    boolean isCurryGenerated() {
        return curryGenerated;
    }
        
	public Expression getDefaultExpression(String paramName) {
		return defaults == null ? null : defaults.get(paramName);
	}
	
	public void fillInDefaults(Scope scope) throws TemplateException {
		for (String paramName : params) {
			TemplateModel arg = scope.get(paramName);
			if (arg == null || arg == TemplateModel.JAVA_NULL) {
				Expression defaultExp = defaults == null ? null : defaults.get(paramName);
				if (defaultExp != null) {
					TemplateModel value = defaultExp.getAsTemplateModel(scope.getEnvironment());
					scope.put(paramName, value);
				}
				else if (arg == null) {
					throw new TemplateModelException("Missing required parameter " + paramName);
				}
			}
		}
	}
	
	List<TemplateModel> getParameterSequence(PositionalArgsList args, Environment env) throws TemplateException {
		List<TemplateModel> result = new ArrayList<TemplateModel>();
		int i=0;
		for (String paramName : params) {
			if (i < args.size()) {
				result.add(args.getValueAt(i, env));
			} else {
				Expression defaultExp = null;
				if (defaults != null) defaultExp = defaults.get(paramName);
				if (defaultExp == null) {
					String msg = "Missing required parameter: " + paramName;
					throw new TemplateException(msg, env);
				}
				TemplateModel defaultValue = defaultExp.getAsTemplateModel(env);
				assertIsDefined(defaultValue, defaultExp, env);
				result.add(defaultValue);
			}
			++i;
		}
		if (args.size() > params.size()) {
			if (catchall == null) {
				throw new TemplateException("Extraneous parameters provided; expected " + 
                                        params.size() + ", got " + args.size(), env);
			}
			for (i=params.size(); i<args.size(); i++) {
				result.add(args.getValueAt(i, env));
			}
		}
		return result;
	}
	
	
	List<TemplateModel> getParameterSequence(NamedArgsList args, Environment env) throws TemplateException {
		List<TemplateModel> result = new ArrayList<TemplateModel>();
		Map<String, Expression> argsMap = args.getCopyOfMap();
		for (String paramName : params) {
			Expression argExp = argsMap.remove(paramName);
			if (argExp != null) {
				TemplateModel argModel = argExp.getAsTemplateModel(env);
				assertIsDefined(argModel, argExp, env);
				result.add(argModel);
			} else {
				Expression defaultExp = null;
				if (defaults != null) defaultExp = defaults.get(paramName);
				if (defaultExp == null) {
					throw new TemplateException("missing required parameter: " + paramName, env);
				}
				TemplateModel defaultValue = defaultExp.getAsTemplateModel(env);
				assertIsDefined(defaultValue, defaultExp, env);
				result.add(defaultValue);
			}
		}
		if (!argsMap.isEmpty()) {
			// TODO location info
			StringBuilder msg = new StringBuilder("Extraneous parameter(s) ");
			for (String s : argsMap.keySet()) {
				msg.append(' ').append(s);
			}
			throw new TemplateException(msg.toString(), env);
		}
		return result;
	}
	
	
	
	/**
	 * Given a positional args list, creates a map of key-value pairs based
	 * on the named parameter info encapsulated in this object. 
	 */
	public Map<String, TemplateModel> getParameterMap(PositionalArgsList args, Environment env, boolean ignoreExtraParams) 
	throws TemplateException 
	{
		Map<String, TemplateModel> result = new HashMap<String, TemplateModel>();
		SimpleSequence catchAllVars = null;
		if (catchall != null) {
			catchAllVars = new SimpleSequence();
			result.put(catchall, catchAllVars);
		}
		if (args.size() > params.size()) {
			if (this.catchall == null) {
				// TODO location info
				if (!ignoreExtraParams)
					throw new TemplateException("Expecting exactly " + params.size() + " arguments, received " + args.size() + " parameters.", env);
			} else {
				for (int i=params.size(); i<args.size(); i++) {
					catchAllVars.add(args.getValueAt(i, env));
				}
			}
		}
		for (int i=0; i < params.size(); i++) {
			String paramName = params.get(i);
			if (i<args.size()) {
				result.put(paramName, args.getValueAt(i, env));
			} 
			else {
				Expression defaultExp = defaults.get(paramName);
				if (defaultExp == null) {
					throw new TemplateException("Parameter " + paramName + " unspecified.", env); 
				}
				TemplateModel defaultValue = defaultExp.getAsTemplateModel(env);
				assertIsDefined(defaultValue, defaultExp, env);
				result.put(paramName, defaultValue);
			}
		}
		return result;
	}
	
	public Map<String, TemplateModel> getParameterMap(ArgsList args, Environment env) throws TemplateException {
		if (args instanceof NamedArgsList) {
			return getParameterMap((NamedArgsList) args, env);
		}
		return getParameterMap((PositionalArgsList) args, env, false);
	}
	
	public Map<String, TemplateModel> getParameterMap(NamedArgsList args, Environment env) 
	throws TemplateException 
	{
		Map<String, TemplateModel> result = new HashMap<String, TemplateModel>();
		Map<String, Expression> argsMap = args.getCopyOfMap();
		for (String paramName : params) {
			Expression argExp = argsMap.remove(paramName);
			if (argExp != null) {
				TemplateModel value = argExp.getAsTemplateModel(env);
				TemplateNode.assertIsDefined(value, argExp, env);
				result.put(paramName, value);
			}
			else {
				Expression defaultExp = defaults.get(paramName);
				if (defaultExp == null) {
					//TODO location info.
					throw new TemplateException("Missing required parameter " + paramName, env);
				}
				TemplateModel value = defaultExp.getAsTemplateModel(env);
				assertIsDefined(value, defaultExp, env);
				result.put(paramName, value);
			}
		}
		SimpleHash catchAllMap = null;
		if (catchall != null) {
			catchAllMap = new SimpleHash();
			result.put(catchall, catchAllMap);
			
		}
		if (!argsMap.isEmpty()) {
                    if(catchall != null || curryGenerated) {
                        for (Map.Entry<String, Expression> entry : argsMap.entrySet()) {
                            Expression exp = entry.getValue();
                            TemplateModel val = exp.getAsTemplateModel(env);
                            assertIsDefined(val, exp, env);
                            if(curryGenerated) {
                                result.put(entry.getKey(), val);
                            } else {
                                catchAllMap.put(entry.getKey(), val);
                            }
                        }
                    } else {
                        throw new TemplateException("Extraneous parameters " + 
                                argsMap.keySet() + " provided.", env);
                    }
		}
		return result;
	}
        
        String toDebugString() {
            StringBuilder b = new StringBuilder();
            for(String argName: params) {
                b.append(argName);
                Expression exp = getDefaultExpression(argName);
                if(exp != null) {
                    b.append('=');
                    b.append(exp);
                }
                b.append(' ');
            }
            return b.toString();
        }

		public TemplateElement getParent() {
		    return parent;
		}
}
