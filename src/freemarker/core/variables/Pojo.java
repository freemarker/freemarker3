package freemarker.core.variables;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static freemarker.core.variables.Wrap.wrap;

/**
 * A class that will wrap an arbitrary POJO (a.k.a. Plain Old Java Object
 */
public class Pojo implements WrappedVariable {
    private Object object;

    private static Map<String, Method> getterSetterCache = new ConcurrentHashMap<>();

    public Pojo(Object object) {
        assert !(object instanceof WrappedVariable) : "The object is already \"wrapped\"!";
        this.object = object;
    }

    public Object get(String key) {
        Method getter = getGetter(key);
        if (getter != null)
            try {
                return wrap(getter.invoke(object));
            } catch (Exception e) {
                throw new EvaluationException(e);
            }
        if (methodOfNameExists(key)) {
            return new JavaMethodCall(object, key);
        }
        return null;
    }

    private Method getGetter(String name) {
        Method cachedMethod = getterSetterCache.get(getLookupKey(name));
        if (cachedMethod != null) {
            return cachedMethod;
        }
        String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
        try {
            Method m = object.getClass().getMethod(methodName);
            if (m.getReturnType() != Void.TYPE) {
                getterSetterCache.put(getLookupKey(name), m);
                return m;
            }
        } catch (NoSuchMethodException nsme) {
        }
        methodName = methodName.replaceFirst("get", "is");
        try {
            Method m = object.getClass().getMethod(methodName);
            if (m.getReturnType() == Boolean.TYPE || m.getReturnType() == Boolean.class) {
                getterSetterCache.put(getLookupKey(name), m);
                return m;
            }
        } catch (NoSuchMethodException nsme) {
        }
        return null;
    }

    private String getLookupKey(String propertyName) {
        return object.getClass().getName() + "#" + propertyName;
    }

    private boolean methodOfNameExists(String name) {
        for (Method m : object.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Object getWrappedObject() {
        return object;
    }

    public int size() {
        if (object instanceof Collection) {
            return ((Collection<?>) object).size();
        }
        if (object instanceof Map) {
            return ((Map<?, ?>) object).size();
        }
        if (object.getClass().isArray()) {
            return Array.getLength(object);
        }
        return -1; // REVISIT
    }

    public String toString() {
        return object.toString();
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other instanceof Pojo) {
            return getWrappedObject().equals(((Pojo) other).getWrappedObject());
        }
        return false;
    }
}