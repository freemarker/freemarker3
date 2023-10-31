package freemarker.core.variables;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static freemarker.core.variables.Wrap.*;
import static freemarker.core.variables.Invoke.*;

/**
 * A class that will wrap an arbitrary POJO (a.k.a. Plain Old Java Object)
 * allowing calls to arbitrary property getters and invocation of
 * accessible methods on the object from a template using the
 * <tt>object.foo</tt> to access properties and <tt>object.bar(arg1, arg2)</tt>
 * to
 * invoke methods on it. You can also use the <tt>object.foo[index]</tt> syntax
 * to
 * access indexed properties. It uses Beans {@link java.beans.Introspector}
 * to dynamically discover the properties and methods.
 * 
 * @author Attila Szegedi
 * @version $Id: BeanModel.java,v 1.51 2006/03/15 05:01:12 revusky Exp $
 */

public class Pojo  {
    protected final Object object;

    // Cached template models that implement member properties and methods for this
    // instance. Keys are FeatureDescriptor instances (from classCache values),
    // values are either ReflectionMethodModels/ReflectionScalarModels
    private HashMap<Object, Object> memberMap;

    public Pojo(Object object) {
        assert !(object instanceof WrappedVariable || object instanceof Pojo || object instanceof Number);
        this.object = object;
    }

    /**
     * Uses Beans introspection to locate a property or method with name
     * matching the key name. If a method or property is found, it is wrapped
     * into {@link freemarker.template.WrappedMethod} (for a method or
     * indexed property), or evaluated on-the-fly and the return value wrapped
     * into appropriate model (for a simple property) Models for various
     * properties and methods are cached on a per-class basis, so the costly
     * introspection is performed only once per property or method of a class.
     * (Side-note: this also implies that any class whose method has been called
     * will be strongly referred to by the framework and will not become
     * unloadable until this class has been unloaded first. Normally this is not
     * an issue, but can be in a rare scenario where you create many classes on-
     * the-fly. Also, as the cache grows with new classes and methods introduced
     * to the framework, it may appear as if it were leaking memory. The
     * framework does, however detect class reloads (if you happen to be in an
     * environment that does this kind of things--servlet containers do it when
     * they reload a web application) and flushes the cache. If no method or
     * property matching the key is found, the framework will try to invoke
     * methods with signature
     * <tt>non-void-return-type get(java.lang.String)</tt>,
     * then <tt>non-void-return-type get(java.lang.Object)</tt>, or
     * alternatively (if the wrapped object is a resource bundle)
     * <tt>Object getObject(java.lang.String)</tt>.
     * 
     * @throws EvaluationException if there was no property nor method nor
     *                             a generic <tt>get</tt> method to invoke.
     */
    public Object get(String key) {
        Class<?> clazz = object.getClass();
        Map<Object, Object> classInfo = getClassKeyMap(clazz);
        Object retval = null;

        introspectClass(object.getClass());
        try {
            Object fd = classInfo.get(key);
            if (fd != null) {
                retval = invokeThroughDescriptor(fd, classInfo);
            } else {
                retval = invokeGenericGet(classInfo, key);
            }
            if (retval == null) {
                // REVISIT
                //throw new InvalidReferenceException("No such bean property: " + key, null);
            }
            return retval;
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException("get(" + key + ") failed on " +
                    "instance of " + object.getClass().getName(), e);
        }
    }

    private Object invokeThroughDescriptor(Object desc, Map classInfo)
            throws IllegalAccessException, InvocationTargetException {
        // See if this particular instance has a cached implementation
        // for the requested feature descriptor
        Object member;
        synchronized (this) {
            if (memberMap != null) {
                member = memberMap.get(desc);
            } else {
                member = null;
            }
        }
        if (member != null)
            return member;
        Object retval = null;
        if (desc instanceof IndexedPropertyDescriptor) {
            Method readMethod = ((IndexedPropertyDescriptor) desc).getIndexedReadMethod();
            retval = member = new SimpleMethodModel(object, readMethod, getArgTypes(classInfo, readMethod));
        } else if (desc instanceof PropertyDescriptor) {
            PropertyDescriptor pd = (PropertyDescriptor) desc;
            retval = invokeMethod(object, pd.getReadMethod(), null);
            // (member == null) condition remains, as we don't cache these
        } else if (desc instanceof Field) {
            retval = wrap(((Field) desc).get(object));
            // (member == null) condition remains, as we don't cache these
        } else if (desc instanceof Method) {
            Method method = (Method) desc;
            retval = member = new SimpleMethodModel(object, method, getArgTypes(classInfo, method));
        } else if (desc instanceof MethodMap) {
            retval = member = new OverloadedMethodModel(object, (MethodMap<Method>) desc);
        }

        // If new cacheable member was created, cache it
        if (member != null) {
            synchronized (this) {
                if (memberMap == null) {
                    memberMap = new HashMap<Object, Object>();
                }
                memberMap.put(desc, member);
            }
        }
        return retval;
    }

    protected Object invokeGenericGet(Map keyMap, String key) throws IllegalAccessException,
            InvocationTargetException {
        Method genericGet = (Method) keyMap.get(GENERIC_GET_KEY);
        if (genericGet == null)
            return null;

        return invokeMethod(object, genericGet, new Object[] { key });
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
        // return getWrappedObject().equals(other);
        return false;
    }
}