package freemarker.core.variables;

import static freemarker.core.variables.Constants.JAVA_NULL;
import static freemarker.core.variables.Constants.NOTHING;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JavaMethodCall implements WrappedMethod {

    private static final Object CAN_NOT_UNWRAP = new Object();    

    private String methodName;
    private Object target;
    private List<Method> possibleMethods;

    private static Map<String,Method> methodCache = new ConcurrentHashMap<>();

    public JavaMethodCall(Object target, String methodName) {
        if (target instanceof Pojo) {
            target = ((Pojo)target).getWrappedObject();
        }
        this.target = target;
        this.methodName = methodName;
        findPossibleMethods();
    }

    public String getMethodName() {
        return methodName;
    }

    public Object getTarget() {
        return target;
    }

    private void findPossibleMethods() {
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
            // call it and that's that! This is the percentage case, after all.
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
                types[i] = Wrap.unwrap(params.get(i)).getClass();
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
        Method compatibleMethod = null;
        boolean hasVarArgs = false;
        for (Method m : possibleMethods) {
            if (!m.isVarArgs() && isCompatibleMethod(m, params)) {
                compatibleMethod = m;
                break;
            }
            if (m.isVarArgs()) {
                hasVarArgs = true;
            }
        }
        if (hasVarArgs && compatibleMethod == null) {
            for (Method m : possibleMethods) {
                if (m.isVarArgs() && isCompatibleMethod(m, params)) {
                    compatibleMethod = m;
                    break;
                }
            }
        }
        if (compatibleMethod == null) {
            throw new EvaluationException("Cannot invoke method " + methodName + " here.");
        }
        return invokeMethod(compatibleMethod, params);
    }

    private boolean isCompatibleMethod(Method method, List<Object> params) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (!method.isVarArgs() && paramTypes.length != params.size()) {
            return false;
        } else if (method.isVarArgs() && params.size() < paramTypes.length-1 ) {
            return false;
        }
        int paramTypesToCheck = paramTypes.length;
        if (method.isVarArgs()) paramTypesToCheck--;
        for (int i = 0; i< paramTypesToCheck; i++) {
            Object arg = unwrap(params.get(i), paramTypes[i]);
            if (arg == CAN_NOT_UNWRAP) {
                return false;
            }
        }
        if (!method.isVarArgs()) return true;
        Class<?> varArgsType = paramTypes[paramTypes.length-1].getComponentType();
        for (int i = paramTypes.length-1; i<params.size();i++) {
            Object arg = unwrap(params.get(i), varArgsType);
            if (arg == CAN_NOT_UNWRAP) return false;
        }
        return true;
    }

    private Object[] unwrapArgsForMethod(Method method, List<Object> params) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        int numFixedParams = paramTypes.length;
        if (method.isVarArgs()) {
            numFixedParams--;
        }
        for (int i = 0; i< numFixedParams; i++) {
            Object param = params.get(i);
            Class<?> paramType = paramTypes[i];
            args[i] = unwrap(param, paramType);
        }
        if (method.isVarArgs()) {
            Class<?> varArgType = paramTypes[paramTypes.length-1].getComponentType();
            Object varArgsArray = Array.newInstance(varArgType, params.size() - numFixedParams);
            for (int i = numFixedParams; i<params.size(); i++) {
                Object arg = unwrap(params.get(i), varArgType);
                Array.set(varArgsArray, i-numFixedParams, arg);
            }
            args[args.length-1] = varArgsArray;
        }
        return args;
    }

    private Object invokeMethod(Method method, List<Object> params) {
        if (isBannedMethod(method)) {
            throw new EvaluationException("Cannot run method: " + method);
        }
        Object[] args = unwrapArgsForMethod(method, params);
        Object result = null;
        try {
           result =  method.invoke(target, args);
        } catch (Exception e) {
            throw new EvaluationException("Error invoking method " + method, e);
        }
        methodCache.put(getLookupKey(params), method);
        if (result == null && method.getReturnType() == Void.TYPE) {
            result = NOTHING;
        }
        return result;
    }

    private String getLookupKey(List<Object> params) {
        StringBuilder buf = new StringBuilder();
        buf.append(target.getClass().getName());
        buf.append('#');
        buf.append(methodName);
        buf.append('#');
        if (params != null) for (Object param : params) {
            buf.append(param.getClass());
            buf.append(':');
        }
        return buf.toString();
    }

    // For now, this is good enough, I reckon.
    private static boolean isBannedMethod(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        if (clazz == Object.class) {
            if (method.getName().equals("wait") || method.getName().startsWith("notify")) {
                return true;
            }
        }
        return clazz == java.lang.System.class
               || clazz == Runtime.class
               || clazz == Thread.class
               || clazz == ThreadGroup.class
               || clazz == Class.class
               || clazz == ClassLoader.class
               || clazz.getPackage().getName().equals("java.lang.reflect");
    }

    private static Object unwrap(Object object, Class<?> desiredType) {
        if (object == null || object == JAVA_NULL || object == NOTHING) {
            return desiredType.isPrimitive() ? CAN_NOT_UNWRAP : null;
        }
        object = Wrap.unwrap(object);
        if (desiredType.isInstance(object)) {
            return object;
        }
        if (desiredType == Boolean.TYPE || desiredType == Boolean.class) {
            if (object instanceof Boolean) {
                return (Boolean) object;
            }
            if (object instanceof WrappedBoolean) {
                return ((WrappedBoolean) object).getAsBoolean();
            }
            return CAN_NOT_UNWRAP;
        }
        if (object instanceof Number) {
            Number num = (Number) object;
            if (desiredType == Integer.class || desiredType == Integer.TYPE) {
                return num.intValue();
            }
            if (desiredType == Long.class || desiredType == Long.TYPE) {
                return num.longValue();
            }
            if (desiredType == Short.class || desiredType == Short.TYPE) {
                return num.shortValue();
            }
            if (desiredType == Byte.class || desiredType == Byte.TYPE) {
                return num.byteValue();
            }
            if (desiredType == Float.class || desiredType == Float.TYPE) {
                return num.floatValue();
            }
            if (desiredType == Double.class || desiredType == Double.TYPE) {
                return num.doubleValue();
            }
            if (desiredType == BigDecimal.class) {
                return new BigDecimal(num.toString());
            }
            if (desiredType == BigInteger.class) {
                return new BigInteger(num.toString());
            }
            return CAN_NOT_UNWRAP;
        }
        if (desiredType == String.class) { 
            //This is not really 100% maybe, but 
            // for now is necessary to keep CongoCC templates
            // all working! Will REVISIT later
            return object.toString();
        }
        if (desiredType == Date.class && object instanceof WrappedDate) {
            // REVISIT
            return ((WrappedDate) object).getAsDate();
        }
        return CAN_NOT_UNWRAP;
    }
}