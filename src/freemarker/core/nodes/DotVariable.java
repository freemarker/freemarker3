package freemarker.core.nodes;

import static freemarker.core.variables.Constants.JAVA_NULL;
import static freemarker.core.variables.Wrap.wrap;
import freemarker.core.variables.scope.Scope;
import freemarker.core.variables.EvaluationException;
import freemarker.core.variables.JavaMethodCall;
import freemarker.core.variables.WrappedHash;
import freemarker.core.parser.Token;
import freemarker.core.Environment;
import freemarker.core.nodes.generated.Expression;
import freemarker.core.nodes.generated.TemplateNode;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import java.util.*;


public class DotVariable extends TemplateNode implements Expression {

    private static Map<String, Method> getterSetterCache = new ConcurrentHashMap<>();
    private static Map<String, Boolean> classHasMethodCache = new ConcurrentHashMap<>();

    public Expression getTarget() {
        return (Expression) get(0);
    }

    public String getKey() {
        return get(2).toString();
    }

    public Object evaluate(Environment env) {
        Object lhs = getTarget().evaluate(env);
        assertNonNull(lhs, env);
        if (lhs instanceof Map) {
//            return wrap(((Map) lhs).get(getKey()));
            Map map = (Map) lhs;
            Object result = map.get(getKey());
            if (result == null) {
                return map.containsKey(getKey()) ? JAVA_NULL : null;
            }
            return wrap(result);
        }
        if (lhs instanceof WrappedHash) {
            return wrap(((WrappedHash) lhs).get(getKey()));
        }
        if (lhs instanceof Scope) {
            return wrap(((Scope) lhs).get(getKey()));
        }
        return get(lhs, getKey(), getTemplate().legacySyntax());
    }

    public static Object get(Object object, String key, boolean looseSyntax) {
        Method getter = getGetter(object, key);
        if (getter != null)
            try {
                return wrap(getter.invoke(object));
            } catch (Exception e) {
                throw new EvaluationException(e);
            }
        if (looseSyntax && methodOfNameExists(object, key)) {
            return new JavaMethodCall(object, key);
        }
        return null;
    }

    private static Method getGetter(Object object, String name) {
        Method cachedMethod = getterSetterCache.get(getLookupKey(object, name));
        if (cachedMethod != null) {
            return cachedMethod;
        }
        String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
        try {
            Method m = object.getClass().getMethod(methodName);
            if (m.getReturnType() != Void.TYPE) {
                getterSetterCache.put(getLookupKey(object, name), m);
                return m;
            }
        } catch (NoSuchMethodException nsme) {
        }
        methodName = methodName.replaceFirst("get", "is");
        try {
            Method m = object.getClass().getMethod(methodName);
            if (m.getReturnType() == Boolean.TYPE || m.getReturnType() == Boolean.class) {
                getterSetterCache.put(getLookupKey(object, name), m);
                return m;
            }
        } catch (NoSuchMethodException nsme) {
        }
        return null;
    }

    private static String getLookupKey(Object object, String propertyName) {
        return object.getClass().getName() + "#" + propertyName;
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


    public Expression _deepClone(String name, Expression subst) {
        Expression clonedTarget = getTarget().deepClone(name, subst);
        Token op = (Token) get(1);
        Token key = (Token) get(2);
        Expression result = new DotVariable();
        result.add(clonedTarget);
        result.add(op);
        result.add(key);
        return result;
    }
}


