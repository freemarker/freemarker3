package freemarker.core.variables;

import java.util.*;
import java.lang.reflect.Array;
import freemarker.core.Environment;
import freemarker.core.nodes.generated.Expression;
import freemarker.core.variables.scope.Scope;
import freemarker.template.TemplateException;

import org.w3c.dom.Node;
import static freemarker.core.variables.Constants.JAVA_NULL;

public class Wrap {

    private static int defaultDateType = WrappedDate.UNKNOWN;

    private Wrap() {}

    public static boolean isMap(Object obj) {
        if (obj instanceof WrappedVariable) {
            obj = ((WrappedVariable) obj).getWrappedObject();
        }
        return obj instanceof Map;
    }

    public static boolean isList(Object obj) {
        if (obj instanceof WrappedSequence) {
            return true;
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        if (obj.getClass().isArray()) {
            return true;
        }
        return obj instanceof List;
    }

    public static List<?> asList(Object obj) {
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        if (obj instanceof WrappedSequence) {
            WrappedSequence tsm = (WrappedSequence) obj;
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < tsm.size(); i++)
                result.add(tsm.get(i));
            return result;
        }
        if (obj.getClass().isArray()) {
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < Array.getLength(obj); i++) {
                result.add(Array.get(obj, i));
            }
            return result;
        }
        return (List<?>) obj;
    }

    public static boolean isNumber(Object obj) {
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        if (obj instanceof Number) {
            return true;
        }
        return false;
    }

    public static boolean isDate(Object obj) {
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        if (obj instanceof Date) {
            return true;
        }
        if (obj instanceof WrappedDate) {
            return true;
        }
        return false;
    }

    public static Number asNumber(Object obj) {
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return (Number) obj;
    }

    public static Date asDate(Object obj) {
        if (obj instanceof WrappedDate) {
            return ((WrappedDate) obj).getAsDate();
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return (Date) obj;
    }

    public static boolean isString(Object obj) {
        if (obj instanceof WrappedString) {
            return true;
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return obj instanceof CharSequence;
    }

    public static String asString(Object obj) {
        if (obj instanceof WrappedString) {
            return ((WrappedString) obj).getAsString();
        }
        return obj.toString();
    }

    public static boolean isDisplayableAsString(Object tm) {
        return isString(tm)
                || tm instanceof Pojo
                || isNumber(tm)
                || tm instanceof WrappedDate;
    }

    public static boolean isBoolean(Object obj) {
        if (obj instanceof WrappedBoolean) {
            return true;
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return obj instanceof Boolean;
    }

    public static boolean asBoolean(Object obj) {
        if (obj instanceof WrappedBoolean) {
            return ((WrappedBoolean) obj).getAsBoolean();
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return (Boolean) obj;
    }

    public static boolean isIterable(Object obj) {
        if (obj instanceof WrappedSequence)
            return true;
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        if (obj.getClass().isArray())
            return true;
        return obj instanceof Iterable || obj instanceof Iterator;
    }

    public static Iterator<?> asIterator(Object obj) {
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        if (obj instanceof Iterator) {
            return (Iterator<?>) obj;
        }
        if (obj.getClass().isArray()) {
            final Object arr = obj;
            return new Iterator<Object>() {
                int index = 0;

                public boolean hasNext() {
                    return index < Array.getLength(arr);
                }

                public Object next() {
                    return Array.get(arr, index++);
                }
            };
        }
        if (obj instanceof WrappedSequence) {
            final WrappedSequence seq = (WrappedSequence) obj;
            return new Iterator<Object>() {
                int index = 0;

                public boolean hasNext() {
                    return index < seq.size();
                }

                public Object next() {
                    return seq.get(index++);
                }
            };
        }
        return ((Iterable<?>) obj).iterator();
    }

    /**
     * Sets the default date type to use for date models that result from
     * a plain <tt>java.util.Date</tt> instead of <tt>java.sql.Date</tt> or
     * <tt>java.sql.Time</tt> or <tt>java.sql.Timestamp</tt>. Default value is
     * {@link WrappedDate#UNKNOWN}.
     * 
     * @param defaultDateType the new default date type.
     */
    public static synchronized void setDefaultDateType(int defaultDateType) {
        Wrap.defaultDateType = defaultDateType;
    }

    static synchronized int getDefaultDateType() {
        return defaultDateType;
    }

    public static Object wrap(Object object) {
        if (object == null) {
            return Constants.JAVA_NULL;
        }
        if (isMarkedAsPojo(object.getClass())) {
            return new Pojo(object);
        }
        if (object instanceof WrappedVariable
                || object instanceof String
                || object instanceof Pojo
                || object instanceof Scope
                || object instanceof Boolean
                || object instanceof Number
                || object instanceof Iterator
                || object instanceof Enumeration) {
            return object;
        }
        if (object instanceof CharSequence) {
            return object.toString(); // REVISIT
        }
        if (object instanceof List) {
            return object;
            //return new Pojo(object);
        }
        if (object instanceof Map) {
            return object;
        }
        if (object.getClass().isArray()) {
            return new Pojo(object);
        }
        if (object instanceof Date) {
            return new DateModel((Date) object);
        }
        if (object instanceof ResourceBundle) {
            return new ResourceBundleModel((ResourceBundle) object);
        }
        return new Pojo(object);
    }

    private static Map<Class<?>, Boolean> markedAsPojoLookup = new HashMap<>();

    private static boolean isMarkedAsPojo(Class<?> clazz) {
        Boolean lookupValue = markedAsPojoLookup.get(clazz);
        if (lookupValue != null)
            return lookupValue;
        if (clazz.getAnnotation(freemarker.annotations.Pojo.class) != null) {
            markedAsPojoLookup.put(clazz, true);
            return true;
        }
        for (Class<?> interf : clazz.getInterfaces()) {
            if (isMarkedAsPojo(interf)) {
                markedAsPojoLookup.put(clazz, true);
                return true;
            }
        }
        if (clazz.getSuperclass() != null) {
            lookupValue = isMarkedAsPojo(clazz.getSuperclass());
        } else {
            lookupValue = false;
        }
        markedAsPojoLookup.put(clazz, lookupValue);
        return lookupValue;
    }

    public static Object unwrap(Object object) {
        if (object == null) {
            throw new EvaluationException("invalid reference");
        }
        if (object == Constants.JAVA_NULL) {
            return null;
        }
        if (object instanceof WrappedVariable) {
            Object unwrapped = ((WrappedVariable) object).getWrappedObject();
            if (unwrapped !=null) {
                return unwrapped;
            }
        }
        return object;
    }

    static public Date getDate(WrappedDate model, Expression expr, Environment env)
    {
        Date value = model.getAsDate();
        if(value == null) {
            throw new TemplateException(expr + " evaluated to null date.", env);
        }
        return value;
    }

    static public Number getNumber(Object model, Expression expr, Environment env)
    {
        if(isNumber(model)) {
            return asNumber(model);
        }
        else if(model == null) {
            throw new InvalidReferenceException(expr + " is undefined.", env);
        }
        else if(model == JAVA_NULL) {
            throw new InvalidReferenceException(expr + " is null.", env);
        }
        else {
            throw new TemplateException(expr + " is not a number, it is " + model.getClass().getName(), env);
        }
    }

    static public Number getNumber(Expression expr, Environment env)
    {
        Object value = expr.evaluate(env);
        return getNumber(value, expr, env);
    }
}