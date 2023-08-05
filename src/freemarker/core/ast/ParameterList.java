package freemarker.core.ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import freemarker.core.helpers.NamedParameterMapScope;
import freemarker.core.parser.ast.BaseNode;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;

/**
 * Represents the specification of the parameters to a macro.
 * Note that we deal with default values and the optional catch-all
 * parameter here (varags...)
 * @version $Id: $
 * @since 2.4
 */

//TODO: Improve the error detail messages maybe

public class ParameterList extends BaseNode {

    List<String> params = new ArrayList<String>();
    private Map<String, Expression> defaults;
    private String catchall;
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

    private boolean hasDefaultExpressions() {
        return defaults != null && !defaults.isEmpty();
    }
    
    public Expression getDefaultExpression(String paramName) {
        return defaults == null ? null : defaults.get(paramName);
    }

    public Expression getDefaultExpression(int paramIndex) {
        if(params == null || paramIndex >= params.size()) {
            return null;
        }
        return getDefaultExpression(params.get(paramIndex));
    }

    private void fillInDefaults(final Environment env, final Scope scope, final Collection<String> paramNames) 
    {
        try {
            env.runInScope(scope, new TemplateRunnable<Object>() {
                public Object run() throws TemplateException, IOException {
                    fillInDefaultsInternal(env, scope, paramNames);
                    return null;
                }
            });
        }
        catch(IOException e) {
            throw new TemplateException(e, env);
        }
    }
    private void fillInDefaultsInternal(Environment env, Scope scope, Collection<String> paramNames) {
        
        boolean resolvedAnArg, hasUnresolvedArg;
        Expression firstUnresolvedExpression;
        InvalidReferenceException firstReferenceException;
        do {
            firstUnresolvedExpression = null;
            firstReferenceException = null;
            resolvedAnArg = hasUnresolvedArg = false;
            for (String paramName : paramNames) {
                Object arg = scope.get(paramName);
                if (arg == null) {
                    Expression defaultExp = getDefaultExpression(paramName);
                    if (defaultExp != null) {
                        try {
                            Object value = defaultExp.getAsTemplateModel(env);
                            if(value == null) {
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

    /**
     * Given a positional list of argument expressions, create a positional 
     * list of template models. Used to pass positional arguments to a template
     * method model.
     */
    List<Object> getParameterSequence(final PositionalArgsList args, 
            final Environment env) 
    {
        final List<Object> result = new ArrayList<>(params.size());
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
        else if(commonSize < paramsSize) {
            // More formal args than actual args -- fill in defaults

            // Create a scope that provides live access to the parameter list
            // so we can reference already defined parameters
            Scope scope = new NamedParameterListScope(env.getCurrentScope(), 
                    params, result, false);
                fillInDefaults(env, scope, params.subList(args.size(), params.size()));
        }
        return result;
    }
    
    /**
     * Given a named list of argument expressions, create a positional 
     * list of template models. Used to pass named arguments to a template
     * method model.
     */
    List<Object> getParameterSequence(final NamedArgsList args, 
            final Environment env) 
    {
        int argsSize = args.size();
        int paramsSize = params.size();
        if(argsSize > paramsSize) {
            Collection<String> l = new LinkedHashSet<String>(args.getArgs().keySet());
            l.removeAll(params);
            throw new TemplateException("Extraneous parameters " + l, env);
        }
        final List<Object> result = new ArrayList<>();
        List<String> unresolvedParamNames = null;
        Map<String, Expression> argsMap = args.getCopyOfMap();
        for (String paramName : params) {
            Expression argExp = argsMap.remove(paramName);
            if (argExp != null) {
                Object argModel = argExp.getAsTemplateModel(env);
                assertIsDefined(argModel, argExp, env);
                result.add(argModel);
            } else {
                if(unresolvedParamNames == null) {
                    unresolvedParamNames = new LinkedList<String>();
                }
                unresolvedParamNames.add(paramName);
            }
        }
        if(unresolvedParamNames != null) {
            // Create a scope that provides live access to the parameter list
            // so we can reference already defined parameters
            final Scope scope = new NamedParameterListScope(
                    env.getCurrentScope(), params, result, false);
            fillInDefaults(env, scope, unresolvedParamNames);
        }
        return result;
    }

    /**
     * Given a positional args list, creates a map of key-value pairs based
     * on the named parameter info encapsulated in this object. 
     */
    public Map<String, Object> getParameterMap(final PositionalArgsList args, 
            final Environment env, boolean ignoreExtraParams) 
    {
        final int argsSize = args.size();
        final int paramsSize = params.size();
        final Map<String, Object> result = new HashMap<>();
        if (catchall == null && argsSize > paramsSize && !ignoreExtraParams) {
            throw new TemplateException("Expecting exactly " + paramsSize + 
                    " arguments, received " + argsSize + ".", env);
        }
        int min = Math.min(paramsSize, argsSize);
        for (int i=0; i < min; i++) {
            result.put(params.get(i), args.getValueAt(i, env));
        }
        if(hasDefaultExpressions() && argsSize < paramsSize) {
            // Create a scope that provides live access to the parameter list
            // so we can reference already defined parameters
            Scope scope = new NamedParameterMapScope(env.getCurrentScope(), 
                    result);
            fillInDefaults(env, scope, params.subList(argsSize, paramsSize));
        }
        if(catchall != null) {
            SimpleSequence catchAllVars = new SimpleSequence();
            result.put(catchall, catchAllVars);
            for (int i = paramsSize; i < argsSize; i++) {
                catchAllVars.add(args.getValueAt(i, env));
            }
        }
        return result;
    }
    
    public Map<String, Object> getParameterMap(NamedArgsList args, Environment env) 
    {
        Map<String, Object> result = new HashMap<>();
        Collection<String> unresolvedParamNames = null;
        Map<String, Expression> argsMap = args.getCopyOfMap();
        for (String paramName : params) {
            Expression argExp = argsMap.remove(paramName);
            if (argExp != null) {
                Object value = argExp.getAsTemplateModel(env);
                BaseNode.assertIsDefined(value, argExp, env);
                result.put(paramName, value);
            }
            else if(defaults != null && defaults.containsKey(paramName)) {
                if(unresolvedParamNames == null) {
                    unresolvedParamNames = new LinkedList<String>();
                }
                unresolvedParamNames.add(paramName);
            }
            else {
                throw new TemplateException("Missing required parameter " + paramName, env);
            }
        }
        if(unresolvedParamNames != null) {
            // Create a scope that provides live access to the parameter list
            // so we can reference already defined parameters
            Scope scope = new NamedParameterMapScope(env.getCurrentScope(), result);
            fillInDefaults(env, scope, unresolvedParamNames);
        }
        SimpleHash catchAllMap = null;
        if (catchall != null) {
            catchAllMap = new SimpleHash();
            result.put(catchall, catchAllMap);
        }
        if (!argsMap.isEmpty()) {
            if(catchall != null) {
                for (Map.Entry<String, Expression> entry : argsMap.entrySet()) {
                    Expression exp = entry.getValue();
                    Object val = exp.getAsTemplateModel(env);
                    assertIsDefined(val, exp, env);
                    catchAllMap.put(entry.getKey(), val);
                }
            } else {
                throw new TemplateException("Extraneous parameters " + 
                        argsMap.keySet() + " provided.", env);
            }
        }
        return result;
    }

    public Map<String, Object> getParameterMapForEmptyArgs(Environment env) 
    {
        Map<String, Object> result = new HashMap<>();
        if(hasDefaultExpressions()) {
            // Create a scope that provides live access to the parameter list
            // so we can reference already defined parameters
            Scope scope = new NamedParameterMapScope(env.getCurrentScope(), 
                    result);
            fillInDefaults(env, scope, defaults.keySet());
        }
        return result;
    }

    public Map<String, Object> getParameterMap(ArgsList args, Environment env) {
        if (args instanceof NamedArgsList) {
            return getParameterMap((NamedArgsList) args, env);
        } 
        if(args instanceof PositionalArgsList) {
            return getParameterMap((PositionalArgsList) args, env, false);
        }
        if (args instanceof EmptyArgsList) {
            return getParameterMapForEmptyArgs(env);
        } 
        throw new AssertionError();
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
