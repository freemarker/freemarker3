package freemarker.core.variables;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import static freemarker.core.variables.Wrap.*;

/**
 * A class that will wrap an arbitrary POJO (a.k.a. Plain Old Java Object
 */
public class Pojo  {
    private Object object;

    public Pojo(Object object) {
        assert !(object instanceof WrappedVariable || object instanceof Pojo || object instanceof Number);
        this.object = object;
    }

    public Object get(String key) {
        Method getter = getGetter(key);
        if (getter != null) try {
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
        if (!Character.isUpperCase(name.charAt(0))) {
            name = name.substring(0,1).toUpperCase() + name.substring(1);
        }
        try {
            Method m = object.getClass().getMethod("get" + name);
            if (m.getReturnType() != Void.TYPE) return m;
        } catch (NoSuchMethodException nsme) {
        }
        try {
            Method m = object.getClass().getMethod("is" + name);
            if (m.getReturnType() == Boolean.TYPE || m.getReturnType() == Boolean.class) return m;
        } catch (NoSuchMethodException nsme) {
        }
        return null; 
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
            return ((Map<?,?>)object).size();
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