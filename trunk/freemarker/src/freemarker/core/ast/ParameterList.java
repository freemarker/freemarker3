package freemarker.core.ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.Scope;
import freemarker.core.TemplateRunnable;
import freemarker.core.helpers.NamedParameterListScope;
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

//TODO: Improve the error detail messages maybe

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

    @Deprecated
    public void fillInDefaults(Scope scope) throws TemplateException {
        fillInDefaults(scope, params);
    }
    
    private void fillInDefaults(Scope scope, Collection<String> paramNames) throws TemplateException {
        boolean resolvedAnArg, hasUnresolvedArg;
        Expression firstUnresolvedExpression;
        InvalidReferenceException firstReferenceException;
        do {
            firstUnresolvedExpression = null;
            firstReferenceException = null;
            resolvedAnArg = hasUnresolvedArg = false;
            for (String paramName : paramNames) {
                TemplateModel arg = scope.get(paramName);
                if (arg == null || arg == TemplateModel.JAVA_NULL) {
                    Expression defaultExp = getDefaultExpression(paramName);
                    if (defaultExp != null) {
                        try {
                            TemplateModel value = defaultExp.getAsTemplateModel(scope.getEnvironment());
                            if(value == null || value == TemplateModel.JAVA_NULL) {
                                if(!hasUnresolvedArg) {
                                    firstUnresolvedExpression = defaultExp;
                                    hasUnresolvedArg = true;
                                }
                            }
                            else {
                                scope.put(paramName, value);
                                resolvedAnArg = true;
                            }
                        }
                        catch(InvalidReferenceException e) {
                            if(!hasUnresolvedArg) {
                                hasUnresolvedArg = true;
                                firstReferenceException = e;
                            }
                        }
                    }
                    else if (arg == null) {
                        throw new TemplateModelException("Missing required parameter " + paramName);
                    }
                }
            }
        }
        while(resolvedAnArg && hasUnresolvedArg);
        if(hasUnresolvedArg) {
            if(firstReferenceException != null) {
                throw firstReferenceException;
            }
            else {
                assert firstUnresolvedExpression != null;
                assertNonNull(null, firstUnresolvedExpression, scope.getEnvironment());
            }
        }
    }

    List<TemplateModel> getParameterSequence(final PositionalArgsList args, 
            final Environment env) 
    throws TemplateException {
        final List<TemplateModel> result = new ArrayList<TemplateModel>(params.size());
        if(defaults == null || defaults.isEmpty() || args.size() >= params.size()) {
            // There'll be no need to fill in defaults, so avoid creating a new
            // temporary scope
            getParameterSequence(result, args, env);
        }
        else {
            // Create a scope that provides live access to the parameter list
            // so we can reference already defined parameters
            try {
                final Scope scope = new NamedParameterListScope(
                        env.getCurrentScope(), params, result, false);
                env.runInScope(scope, new TemplateRunnable<Object>() {
                        public Object run() throws TemplateException {
                            getParameterSequence(result, args, env);
                            fillInDefaults(scope, params.subList(args.size(), 
                                    params.size()));
                            return null;
                        }
                    });
            }
            catch(IOException e) {
                throw new TemplateException(e, env);
            }
        }
        return result;
    }
    
    private void getParameterSequence(List<TemplateModel> result, 
            PositionalArgsList args, Environment env)
    throws TemplateException {
        int argsSize = args.size();
        int paramsSize = params.size();
        int commonSize = Math.min(argsSize, paramsSize);
        // Set formal args that have matching actual args
        for(int i = 0; i < commonSize; ++i) {
            result.add(args.getValueAt(i, env));
        }
        if(commonSize < argsSize) {
            // More actual args than formal args -- use catchall if present
            if (catchall == null) {
                throw new TemplateException("Extraneous parameters provided; expected " + 
                        paramsSize + ", got " + argsSize, env);
            }
            for (int i = commonSize; i < argsSize; i++) {
                result.add(args.getValueAt(i, env));
            }
        }
    }

    List<TemplateModel> getParameterSequence(final NamedArgsList args, 
            final Environment env) 
    throws TemplateException {
        int argsSize = args.size();
        int paramsSize = params.size();
        if(argsSize > paramsSize) {
            Collection<String> l = new LinkedHashSet<String>(args.getArgs().keySet());
            l.removeAll(params);
            throw new TemplateException("Extraneous parameters " + l, env);
        }
        final List<TemplateModel> result = new ArrayList<TemplateModel>();
        if(defaults == null || defaults.isEmpty() || argsSize == paramsSize) {
            // There'll be no need to fill in defaults, so avoid creating a new
            // temporary scope
            getParameterSequence(result, args, env);
        }
        else {
            // Create a scope that provides live access to the parameter list
            // so we can reference already defined parameters
            try {
                final Scope scope = new NamedParameterListScope(
                        env.getCurrentScope(), params, result, false);
                env.runInScope(scope, new TemplateRunnable<Object>() {
                        public Object run() throws TemplateException {
                            fillInDefaults(scope, getParameterSequence(result, 
                                    args, env));
                            return null;
                        }
                    });
            }
            catch(IOException e) {
                throw new TemplateException(e, env);
            }
        }
        return result;
    }

    private List<String> getParameterSequence(List<TemplateModel> result,
            NamedArgsList args, Environment env) throws TemplateException
    {
        List<String> unresolvedParamNames = null;
        Map<String, Expression> argsMap = args.getCopyOfMap();
        for (String paramName : params) {
            Expression argExp = argsMap.remove(paramName);
            if (argExp != null) {
                TemplateModel argModel = argExp.getAsTemplateModel(env);
                assertIsDefined(argModel, argExp, env);
                result.add(argModel);
            } else {
                if(unresolvedParamNames == null) {
                    unresolvedParamNames = new LinkedList<String>();
                }
                unresolvedParamNames.add(paramName);
            }
        }
        if(unresolvedParamNames == null) {
            unresolvedParamNames = Collections.emptyList();
        }
        return unresolvedParamNames;
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
                //assertIsDefined(value, defaultExp, env);
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

    public Map<String, TemplateModel> getParameterMap(ArgsList args, Environment env) throws TemplateException {
        if (args instanceof NamedArgsList) {
            return getParameterMap((NamedArgsList) args, env);
        }
        return getParameterMap((PositionalArgsList) args, env, false);
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
