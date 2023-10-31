package freemarker.core.variables;

import static freemarker.core.variables.Constants.JAVA_NULL;
import static freemarker.core.variables.Wrap.unwrap;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JavaMethodCall implements WrappedMethod {

    private String methodName;
    private Object target;
    private List<Method> possibleMethods;

    private static Map<String,Method> methodCache = new ConcurrentHashMap<>();

    public JavaMethodCall(Object target, String methodName) {
        this.target = target;
        this.methodName = methodName;
        checkMethodName();
    }

    public JavaMethodCall(Object target, Method method) {
        this.target = target;
        possibleMethods = new ArrayList<>();
        possibleMethods.add(method);
    }

    public String getMethodName() {
        return methodName;
    }

    public Object getTarget() {
        return target;
    }

    private void checkMethodName() {
        Class<?> clazz = target.getClass();
        Method[] methods = clazz.getMethods();
        possibleMethods = new ArrayList<>();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                possibleMethods.add(method);
            }
        }
    }

    public boolean isMethodOverloaded() {
        return possibleMethods.size() > 1;
    }

    public boolean isInvalidMethodName() {
        return possibleMethods.size() == 0;
    }

    public Object exec(List<Object> params) {
        if (isInvalidMethodName()) throw new EvaluationException("No such method " + methodName + " in class: " + target.getClass());
        if (params == null) params = new ArrayList<>();
        if (!isMethodOverloaded())  {
            // If there is only one method of this name, just try to
            // call it and that's that!
            return invokeMethod(possibleMethods.get(0), params);
        }
        Method method = methodCache.get(getLookupKey(params));
        if (method != null) {
            // If we have already figured out which method
            // to call and cached it, then we use that! 
            return invokeMethod(method, params);
        }
        // Now we just see if the JVM will tell us the method.
        // We have a problem if we have any nulls because there 
        // can easily be ambiguity, but if not...
        if (!params.contains(JAVA_NULL) && !params.contains(null)) {
            Class<?>[] types = new Class<?>[params.size()];
            for (int i = 0; i<params.size(); i++) {
                types[i] = unwrap(params.get(i)).getClass();
            }
            try {
                method = target.getClass().getMethod(methodName, types);
            } catch (NoSuchMethodException nsme) {
                method = null;
            }
            if (method != null) {
                return invokeMethod(method, params);
            }
        }
        int numParams = params == null ? 0 : params.size();
        List<Method> rightParamNumberMethods = new ArrayList<>();
        for (Method m : possibleMethods) {
            // TODO. Deal with varargs!
            if (m.getParameterTypes().length == numParams) {
                rightParamNumberMethods.add(m);
            }
        }
        if (rightParamNumberMethods.isEmpty()) {
            throw new EvaluationException("Wrong number of parameters");
        }
        if (rightParamNumberMethods.size() == 1) {
            // If there is only one method with the right number of parameters,
            // we try that one!
            return invokeMethod(rightParamNumberMethods.get(0), params);
        }
        for (int i = 0; i< rightParamNumberMethods.size() -1; i++) {
            Method m = rightParamNumberMethods.get(i);
            try {
                return invokeMethod(m, params);
            } catch (EvaluationException e) {}
        }
        return invokeMethod(rightParamNumberMethods.get(rightParamNumberMethods.size()-1), params);
    }

    private Object invokeMethod(Method method, List<Object> params) {
        int numParams = params == null ? 0 : params.size();
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != numParams) {
            throw new EvaluationException("Wrong number of parameters");
        }
        Object[] args = null;
        if (params != null && !params.isEmpty()) {
            args = new Object[params.size()];
            for (int i = 0; i< numParams; i++) {
                Object param = params.get(i);
                Class<?> paramType = paramTypes[i];
                args[i] = Wrap.unwrap(param, paramType);
                if (args[i] == Wrap.CAN_NOT_UNWRAP) {
                    throw new EvaluationException("Wrong parameter type: " + param.getClass());
                }
            }
        }
        Object result = null;
        try {
           result =  method.invoke(target, args);
        } catch (Exception e) {
            throw new EvaluationException("Error invoking method " + method, e);
        }
        methodCache.put(getLookupKey(params), method);
        return result;
    }

    private String getLookupKey(List<Object> params) {
        StringBuilder buf = new StringBuilder();
        buf.append(target.getClass().getName());
        buf.append(':');
        buf.append(methodName);
        buf.append(':');
        if (params != null) for (Object param : params) {
            buf.append(param.getClass());
            buf.append(':');
        }
        return buf.toString();
    }
}