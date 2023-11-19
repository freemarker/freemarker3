package freemarker.core.variables;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;
import java.lang.reflect.Array;
import static freemarker.core.variables.Wrap.*;

/**
 * Code for invoking Java methods by reflection
 */
public class ReflectionCode {

    private static Map<String,Method> methodCache = new ConcurrentHashMap<>();
    private static Map<String, Method> getterCache = new ConcurrentHashMap<>();
    private static Map<String, Method> setterCache = new ConcurrentHashMap<>();
    private static Map<String, Boolean> classHasMethodCache = new ConcurrentHashMap<>();
    private static final Object CAN_NOT_UNWRAP = new Object();
    private static final Method NO_SUCH_METHOD;
    static {
        try {
            NO_SUCH_METHOD = Object.class.getMethod("wait");
        } catch (Exception e) {
            throw new InternalError("This should be impossible!");
        }
    }

    private ReflectionCode() {}

    public static Object invokeMethod(Object target, Method method, List<Object> params) {
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
        if (result == null && method.getReturnType() == Void.TYPE) {
//            result = NOTHING;
            result = null;
        }
        return result;
    }

    public static Object getProperty(Object object, String key, boolean looseSyntax) {
        Method getter = getGetter(object, key);
        if (getter != NO_SUCH_METHOD) {
            try {
                return wrap(getter.invoke(object));
            } catch (Exception e) {
                throw new EvaluationException(e);
            }
        }
        if (looseSyntax && methodOfNameExists(object, key)) {
            return new JavaMethodCall(object, key);
        }
        return null;
    }

    public static boolean setProperty(Object object, String key, Object value) {
        Method setter = getSetter(object, key, value);
        if (setter == NO_SUCH_METHOD) return false;
        Class<?> desiredType = setter.getParameterTypes()[0];
        value = unwrap(value, desiredType);
        try {
           setter.invoke(object, value);
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
        return true;
    }

    static Method getCachedMethod(Object target, String methodName, List<Object> params) {
        return methodCache.get(getLookupKey(target, methodName, params));
    }

    static void cacheMethod(Method m, Object target, List<Object> params) {
        methodCache.put(getLookupKey(target, m.getName(), params), m);
    }

    static boolean isCompatibleMethod(Method method, List<Object> params) {
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

    static boolean isMoreSpecific(Method method1, Method method2, List<Object> params) {
        if (!method1.isVarArgs() && method2.isVarArgs()) return true;
        if (method1.isVarArgs() && !method2.isVarArgs()) return false;
        Class<?>[] types1 = method1.getParameterTypes();
        Class<?>[] types2 = method2.getParameterTypes();
        int numParams = types1.length;
        if (method1.isVarArgs()) --numParams;
        boolean moreSpecific = false, lessSpecific = false;
        for (int i = 0; i < numParams; i++) {
            Class<?> type1 = types1[i];
            Class<?> type2 = types2[i];
            if (type1 == type2) continue;
            Object param = params.get(i);
            if (type1.isInstance(param) && !type2.isInstance(param)) {
                moreSpecific = true;
                continue;
            }
            if (type2.isInstance(param) && !type1.isInstance(param)) {
                lessSpecific = true;
                continue;
            }
            if (type2.isAssignableFrom(type1)) {
                moreSpecific = true;
            } else {
                lessSpecific = true;
            }
        }
        return moreSpecific && !lessSpecific;
    }

    private static boolean methodOfNameExists(Object object, String name) {
        String lookupKey = getLookupKey(object, name);
        Boolean b = classHasMethodCache.get(lookupKey);
        if (b != null) return b;
        for (Method m : object.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                classHasMethodCache.put(lookupKey, true);
                return true;
            }
        }
        classHasMethodCache.put(lookupKey, false);
        return false;
    }

    private static Method getGetter(Object object, String name) {
        String lookupKey = getLookupKey(object, name);
        Method cachedMethod = getterCache.get(lookupKey);
        if (cachedMethod != null) {
            return cachedMethod;
        }
        String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
        try {
            Method m = object.getClass().getMethod(methodName);
            if (m.getReturnType() != Void.TYPE) {
                getterCache.put(lookupKey, m);
                m.setAccessible(true);
                return m;
            }
        } catch (NoSuchMethodException nsme) {
        }
        methodName = methodName.replaceFirst("get", "is");
        try {
            Method m = object.getClass().getMethod(methodName);
            if (m.getReturnType() == Boolean.TYPE || m.getReturnType() == Boolean.class) {
                getterCache.put(getLookupKey(object, name), m);
                m.setAccessible(true);
                return m;
            }
        } catch (NoSuchMethodException nsme) {
        }
        getterCache.put(lookupKey, NO_SUCH_METHOD);
        return NO_SUCH_METHOD;
    }

    private static Method getSetter(Object target, String name, Object value) {
        String lookupKey = getLookupKey(target, name, value);
        Method cachedMethod = setterCache.get(lookupKey);
        if (cachedMethod != null) {
            return cachedMethod;
        }
        String methodName = "set" + name.substring(0,1).toUpperCase() + name.substring(1);
        for (Method m : target.getClass().getMethods()) {
            if (!m.getName().equals(methodName)) continue;
            if (m.getReturnType() != Void.TYPE) continue;
            if (m.getParameterTypes().length != 1) continue;
            Class<?> type = m.getParameterTypes()[0];
            Object unwrapped = unwrap(value, type);
            if (unwrapped != CAN_NOT_UNWRAP) {
                setterCache.put(lookupKey, m);
                m.setAccessible(true);
                return m;
            }
        }
        setterCache.put(lookupKey, NO_SUCH_METHOD);
        return NO_SUCH_METHOD;
    }

    private static Object[] unwrapArgsForMethod(Method method, List<Object> params) {
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
            if (object instanceof Truthy) {
                return ((Truthy) object).getAsBoolean();
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
        if (desiredType == Date.class && object instanceof WrappedDate) {
            // REVISIT
            return ((WrappedDate) object).getAsDate();
        }
        if (desiredType == String.class) { 
            return object.toString();
        }
        return CAN_NOT_UNWRAP;
    }
    
    private static String getLookupKey(Object target, String methodName, List<Object> params) {
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

    private static String getLookupKey(Object object, String propertyName) {
        return object.getClass().getName() + "##" + propertyName;
    }

    private static String getLookupKey(Object object, String propertyName, Object value) {
        return object.getClass().getName() + "#" + propertyName + "#" + value.getClass().getName();
    }
}