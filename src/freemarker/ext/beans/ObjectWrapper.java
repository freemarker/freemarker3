package freemarker.ext.beans;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.beans.*;
import java.io.InputStream;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import freemarker.log.Logger;
import freemarker.template.*;

public class ObjectWrapper 
{
    public static final Object CAN_NOT_UNWRAP = new Object();
    private static final Class<java.math.BigInteger> BIGINTEGER_CLASS = java.math.BigInteger.class;
    private static final Class<Boolean> BOOLEAN_CLASS = Boolean.class;
    private static final Class<Character> CHARACTER_CLASS = Character.class;
    private static final Class<Collection> COLLECTION_CLASS = Collection.class;
    private static final Class<Date> DATE_CLASS = Date.class;
    private static final Class<HashAdapter> HASHADAPTER_CLASS = HashAdapter.class;
    private static final Class<Iterable> ITERABLE_CLASS = Iterable.class;
    private static final Class<Number> NUMBER_CLASS = Number.class;
    private static final Class<Object> OBJECT_CLASS = Object.class;
    private static final Class<SequenceAdapter> SEQUENCEADAPTER_CLASS = SequenceAdapter.class;
    private static final Class<Set> SET_CLASS = Set.class;
    private static final Class<SetAdapter> SETADAPTER_CLASS = SetAdapter.class;
    private static final Class<String> STRING_CLASS = String.class;
    
    // When this property is true, some things are stricter. This is mostly to
    // catch anomalous things in development that can otherwise be valid situations
    // for our users.
    private static final boolean DEVELOPMENT = "true".equals(System.getProperty("freemarker.development"));

    private static final Logger logger = Logger.getLogger("freemarker.beans");
    
    private static final Set<Method> UNSAFE_METHODS = createUnsafeMethodsSet();
    
    static final Object GENERIC_GET_KEY = new Object();
    private static final Object CONSTRUCTORS = new Object();
    //private static final Object ARGTYPES = new Object();
    private static final Map<AccessibleObject, Class<?>[]> ARGTYPES = new HashMap<>();
    
    /**
     * The default instance of BeansWrapper
     */
    private static ObjectWrapper instance;

    // Cache of hash maps that contain already discovered properties and methods
    // for a specified class. Each key is a Class, each value is a hash map. In
    // that hash map, each key is a property/method name, each value is a
    // MethodDescriptor or a PropertyDescriptor assigned to that property/method.
    private final Map<Class<?>,Map<String,Object>> classCache = new ConcurrentHashMap<>();
    private Set<String> cachedClassNames = new HashSet<String>();

    /**
     * At this level of exposure, all methods and properties of the
     * wrapped objects are exposed to the template.
     */
    public static final int EXPOSE_ALL = 0;
    
    /**
     * At this level of exposure, all methods and properties of the wrapped
     * objects are exposed to the template except methods that are deemed
     * not safe. The not safe methods are java.lang.Object methods wait() and
     * notify(), java.lang.Class methods getClassLoader() and newInstance(),
     * java.lang.reflect.Method and java.lang.reflect.Constructor invoke() and
     * newInstance() methods, all java.lang.reflect.Field set methods, all 
     * java.lang.Thread and java.lang.ThreadGroup methods that can change its 
     * state, as well as the usual suspects in java.lang.System and
     * java.lang.Runtime.
     */
    public static final int EXPOSE_SAFE = 1;
    
    /**
     * At this level of exposure, only property getters are exposed.
     * Additionally, property getters that map to unsafe methods are not
     * exposed (i.e. Class.classLoader and Thread.contextClassLoader).
     */
    public static final int EXPOSE_PROPERTIES_ONLY = 2;

    /**
     * At this level of exposure, no bean properties and methods are exposed.
     * Only map items, resource bundle items, and objects retrieved through
     * the generic get method (on objects of classes that have a generic get
     * method) can be retrieved through the hash interface. You might want to 
     * call {@link #setMethodsShadowItems(boolean)} with <tt>false</tt> value to
     * speed up map item retrieval.
     */
    public static final int EXPOSE_NOTHING = 3;

    private int exposureLevel = EXPOSE_SAFE;
    private boolean methodsShadowItems = true;
    private int defaultDateType = TemplateDateModel.UNKNOWN;

    private boolean strict = false;
    
    /**
     * Creates a new instance of BeansWrapper. The newly created instance
     * will use the null reference as its null object, it will use
     * {@link #EXPOSE_SAFE} method exposure level, and will not cache
     * model instances.
     */
    private ObjectWrapper() {}

    public static boolean isMap(Object obj) {
        if (obj instanceof TemplateHashModel) {
            return true;
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo)obj).getWrappedObject();
        }
        return obj instanceof Map;
    }

    public static boolean isList(Object obj) {
        if (obj instanceof TemplateSequenceModel) {
            return true;
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return obj instanceof List;
    }

    public static List asList(Object obj) {
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        if (obj instanceof TemplateSequenceModel) {
            TemplateSequenceModel tsm = (TemplateSequenceModel) obj;
            List result = new ArrayList();
            for (int i = 0; i < tsm.size() ; i++) result.add(tsm.get(i));
            return result;
        }
        return (List) obj;
    }

    public static boolean isNumber(Object obj) {
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        if (obj instanceof Number) {
            return true;
        }
        if (obj instanceof TemplateNumberModel) {
            return true;
        }
        return false;
    }

    public static Number asNumber(Object obj) {
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        if (obj instanceof TemplateNumberModel) {
            return ((TemplateNumberModel) obj).getAsNumber();
        }
        return (Number) obj;
    }

    public static Date asDate(Object obj) {
        if (obj instanceof TemplateDateModel) {
            return ((TemplateDateModel) obj).getAsDate();
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return (Date) obj;
    }

    public static boolean isString(Object obj) {
        if (obj instanceof TemplateScalarModel) {
            return true;
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return obj instanceof CharSequence;
    }

    public static String asString(Object obj) {
        if (obj instanceof TemplateScalarModel) {
            return ((TemplateScalarModel) obj).getAsString();
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return ((CharSequence) obj).toString();
    }

    public static boolean isBoolean(Object obj) {
        if (obj instanceof TemplateBooleanModel) {
            return true;
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return obj instanceof Boolean;
    }

    public static boolean asBoolean(Object obj) {
        if (obj instanceof TemplateBooleanModel) {
            return ((TemplateBooleanModel) obj).getAsBoolean();
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return (Boolean) obj;
    }

    
    /**
     * @see #setStrict(boolean)
     */
    public boolean isStrict() {
    	return strict;
    }
    
    /**
     * Specifies if an attempt to read a bean property that doesn't exist in the
     * wrapped object should throw an {@link InvalidPropertyException}.
     * 
     * <p>If this property is <tt>false</tt> (the default) then an attempt to read
     * a missing bean property is the same as reading an existing bean property whose
     * value is <tt>null</tt>. The template can't tell the difference, and thus always
     * can use <tt>?default('something')</tt> and <tt>?exists</tt> and similar built-ins
     * to handle the situation.
     *
     * <p>If this property is <tt>true</tt> then an attempt to read a bean propertly in
     * the template (like <tt>myBean.aProperty</tt>) that doesn't exist in the bean
     * object (as opposed to just holding <tt>null</tt> value) will cause
     * {@link InvalidPropertyException}, which can't be suppressed in the template
     * (not even with <tt>myBean.noSuchProperty?default('something')</tt>). This way
     * <tt>?default('something')</tt> and <tt>?exists</tt> and similar built-ins can be used to
     * handle existing properties whose value is <tt>null</tt>, without the risk of
     * hiding typos in the property names. Typos will always cause error. But mind you, it
     * goes against the basic approach of FreeMarker, so use this feature only if you really
     * know what are you doing.
     */
    public void setStrict(boolean strict) {
    	this.strict = strict;
    }

    /**
     * Sets the method exposure level. By default, set to <code>EXPOSE_SAFE</code>.
     * @param exposureLevel can be any of the <code>EXPOSE_xxx</code>
     * constants.
     */
    public void setExposureLevel(int exposureLevel)
    {
        if(exposureLevel < EXPOSE_ALL || exposureLevel > EXPOSE_NOTHING)
        {
            throw new IllegalArgumentException("Illegal exposure level " + exposureLevel);
        }
        this.exposureLevel = exposureLevel;
    }
    
    int getExposureLevel()
    {
        return exposureLevel;
    }
    
    /**
     * Sets whether methods shadow items in beans. When true (this is the
     * default value), <code>${object.name}</code> will first try to locate
     * a bean method or property with the specified name on the object, and
     * only if it doesn't find it will it try to call
     * <code>object.get(name)</code>, the so-called "generic get method" that
     * is usually used to access items of a container (i.e. elements of a map).
     * When set to false, the lookup order is reversed and generic get method
     * is called first, and only if it returns null is method lookup attempted.
     */
    public synchronized void setMethodsShadowItems(boolean methodsShadowItems)
    {
        this.methodsShadowItems = methodsShadowItems;
    }
    
    boolean isMethodsShadowItems()
    {
        return methodsShadowItems;
    }
    
    /**
     * Sets the default date type to use for date models that result from
     * a plain <tt>java.util.Date</tt> instead of <tt>java.sql.Date</tt> or
     * <tt>java.sql.Time</tt> or <tt>java.sql.Timestamp</tt>. Default value is 
     * {@link TemplateDateModel#UNKNOWN}.
     * @param defaultDateType the new default date type.
     */
    public synchronized void setDefaultDateType(int defaultDateType) {
        this.defaultDateType = defaultDateType;
    }
    
    protected synchronized int getDefaultDateType() {
        return defaultDateType;
    }
    
    /**
     * Returns the default instance of the wrapper. This instance is used
     * when you construct various bean models without explicitly specifying
     * a wrapper. It is also returned by 
     * and this is the sole instance that is used by the JSP adapter.
     * You can modify the properties of the default instance (caching,
     * exposure level, null model) to affect its operation. By default, the
     * default instance is not caching, uses the <code>EXPOSE_SAFE</code>
     * exposure level, and uses null reference as the null model.
     */
    public static ObjectWrapper instance()
    {
        if (instance == null) {
            instance = new ObjectWrapper();
        }
        return instance;
    }

    public static Object wrap(Object obj) {
        return instance()._wrap(obj);
    }

    /**
     * Wraps the object with a template model that is most specific for the object's
     * class. Specifically:
     * <ul>
     * <li>if the object is null, returns {@link Constants#JAVA_NULL}</li>
     * <li>if the object is already a {@link TemplateModel}, returns it unchanged,</li>
     * <li>if the object is a {@link TemplateModelAdapter}, returns its underlying model,</li>
     * <li>if the object is a Map, returns a {@link SimpleMapModel} for it
     * <li>if the object is a Collection, returns a {@link CollectionModel} for it
     * <li>if the object is an array, returns a {@link ArrayModel} for it
     * <li>if the object is a Number returns a {@link NumberModel} for it,</li>
     * <li>if the object is a Date returns a {@link DateModel} for it,</li>
     * <li>if the object is a Boolean returns 
     * {@link freemarker.template.TemplateBooleanModel#TRUE} or 
     * {@link freemarker.template.TemplateBooleanModel#FALSE}</li>
     * <li>if the object is a ResourceBundle returns a {@link ResourceBundleModel} for it,</li>
     * <li>if the object is an Iterator, returns a {@link IteratorModel} for it
     * <li>if the object is an Enumeration, returns a {@link EnumerationModel} for it
     * <li>otherwise, returns a generic {@link StringModel} for it.
     * </ul>
     */
    public Object _wrap(Object object) 
    {
        if(object == null) {
            return Constants.JAVA_NULL;
        }
        if(object instanceof TemplateModel) {
            return object;
        }
        if (object instanceof Map) {
            return  new SimpleMapModel((Map<?,?>)object);
        }
        if (object instanceof List) {
            return new ListModel((List<?>)object);
        }
        if (object instanceof Iterator) {
            return new IteratorModel((Iterator<?>) object);
        }
        if (object instanceof Enumeration) {
            return new EnumerationModel((Enumeration<?>)object);
        }
        if (object instanceof Boolean) {
            return object;
        }
        if (object.getClass().isArray()) {
            return new ArrayModel(object);
        }
        if(object instanceof TemplateModelAdapter) {
            return ((TemplateModelAdapter)object).getTemplateModel();
        }
        if (object instanceof Number) {
            return new NumberModel((Number)object);
        }
        if (object instanceof Date) {
            return new DateModel((Date) object);
        }
        if (object instanceof Collection) {
            return new CollectionModel((Collection)object);
        }
        if (object instanceof ResourceBundle) {
            return new ResourceBundleModel((ResourceBundle)object);
        }
        if (object instanceof CharSequence) {
            return object;
        }
        return new Pojo(object);
    }

    /**
     * Attempts to unwrap a model into underlying object. Generally, this
     * method is the inverse of the {@link #_wrap(Object)} method. In addition
     * it will unwrap arbitrary {@link TemplateNumberModel} instances into
     * a number, arbitrary {@link TemplateDateModel} instances into a date,
     * {@link TemplateScalarModel} instances into a String, and
     * {@link TemplateBooleanModel} instances into a Boolean.
     * All other objects are returned unchanged.
     */
    public Object unwrap(Object object) 
    {
        return unwrap(object, OBJECT_CLASS);
    }
    
    public Object unwrap(Object object, Class requiredType) 
    {
        return unwrap(object, requiredType, null);
    }
    
    private Object unwrap(Object object, Class<?> requiredType, 
            Map<Object, Object> recursionStops) 
    {
        if(object == null) {
            throw new TemplateModelException("invalid reference");
        }

        if (object == Constants.JAVA_NULL) {
            return null;
        }

        if (!(object instanceof TemplateModel)) {
            return object;
        }
        
        boolean isBoolean = Boolean.TYPE == requiredType;
        boolean isChar = Character.TYPE == requiredType;
        
        // This is for transparent interop with other wrappers (and ourselves)
        // Passing the hint allows i.e. a Jython-aware method that declares a
        // PyObject as its argument to receive a PyObject from a JythonModel
        // passed as an argument to TemplateMethodModelEx etc.
        if(object instanceof AdapterTemplateModel) {
            Object adapted = ((AdapterTemplateModel)object).getAdaptedObject(
                    requiredType);
            if(requiredType.isInstance(adapted)) {
                return adapted;
            }
            // Attempt numeric conversion 
            if(adapted instanceof Number && ((requiredType.isPrimitive() && !isChar && 
                    !isBoolean) || NUMBER_CLASS.isAssignableFrom(requiredType))) {
                Number number = convertUnwrappedNumber(requiredType,
                        (Number)adapted);
                if(number != null) {
                    return number;
                }
            }
        }
        
        // Translation of generic template models to POJOs. First give priority
        // to various model interfaces based on the hint class. This helps us
        // select the appropriate interface in multi-interface models when we
        // know what is expected as the return type.

        if(STRING_CLASS == requiredType) {
            if(object instanceof TemplateScalarModel) {
                return ((TemplateScalarModel)object).getAsString();
            }
            // String is final, so no other conversion will work
            return CAN_NOT_UNWRAP;
        }

        // Primitive numeric types & Number.class and its subclasses
        if((requiredType.isPrimitive() && !isChar && !isBoolean) 
                || NUMBER_CLASS.isAssignableFrom(requiredType)) {
            if(object instanceof TemplateNumberModel) {
                Number number = convertUnwrappedNumber(requiredType, 
                        ((TemplateNumberModel)object).getAsNumber());
                if(number != null) {
                    return number;
                }
            }
        }
        
        if(isBoolean || BOOLEAN_CLASS == requiredType) {
            if(object instanceof TemplateBooleanModel) {
                return ((TemplateBooleanModel)object).getAsBoolean() 
                ? Boolean.TRUE : Boolean.FALSE;
            }
            // Boolean is final, no other conversion will work
            return CAN_NOT_UNWRAP;
        }

        if(requiredType == Map.class) {
            if(object instanceof TemplateHashModel) {
                return new HashAdapter((TemplateHashModel)object, this);
            }
        }
        
        if(requiredType == List.class) {
            if(object instanceof TemplateSequenceModel) {
                return new SequenceAdapter((TemplateSequenceModel)object, this);
            }
        }
        
        if(SET_CLASS == requiredType) {
            if(object instanceof TemplateCollectionModel) {
                return new SetAdapter((TemplateCollectionModel)object, this);
            }
        }
        
        if(COLLECTION_CLASS == requiredType 
                || ITERABLE_CLASS == requiredType) {
            if(object instanceof TemplateCollectionModel) {
                return new CollectionAdapter((TemplateCollectionModel)object, 
                        this);
            }
            if(object instanceof TemplateSequenceModel) {
                return new SequenceAdapter((TemplateSequenceModel)object, this);
            }
        }
        
        // TemplateSequenceModels can be converted to arrays
        if(requiredType.isArray()) {
            if(object instanceof TemplateSequenceModel) {
                if(recursionStops != null) {
                    Object retval = recursionStops.get(object);
                    if(retval != null) {
                        return retval;
                    }
                } else {
                    recursionStops = 
                        new IdentityHashMap<Object, Object>();
                }
                TemplateSequenceModel seq = (TemplateSequenceModel)object;
                Class componentType = requiredType.getComponentType();
                Object array = Array.newInstance(componentType, seq.size());
                recursionStops.put(object, array);
                try {
                    int size = seq.size();
                    for (int i = 0; i < size; i++) {
                        Object val = unwrap(seq.get(i), componentType, 
                                recursionStops);
                        if(val == CAN_NOT_UNWRAP) {
                            return CAN_NOT_UNWRAP;
                        }
                        Array.set(array, i, val);
                    }
                } finally {
                    recursionStops.remove(object);
                }
                return array;
            }
            // array classes are final, no other conversion will work
            return CAN_NOT_UNWRAP;
        }
        
        // Allow one-char strings to be coerced to characters
        if(isChar || requiredType == CHARACTER_CLASS) {
            if(object instanceof TemplateScalarModel) {
                String s = ((TemplateScalarModel)object).getAsString();
                if(s.length() == 1) {
                    return Character.valueOf(s.charAt(0));
                }
            }
            // Character is final, no other conversion will work
            return CAN_NOT_UNWRAP;
        }

        if(DATE_CLASS.isAssignableFrom(requiredType)) {
            if(object instanceof TemplateDateModel) {
                Date date = ((TemplateDateModel)object).getAsDate();
                if(requiredType.isInstance(date)) {
                    return date;
                }
            }
        }
        
        // Translation of generic template models to POJOs. Since hint was of
        // no help initially, now use an admittedly arbitrary order of 
        // interfaces. Note we still test for isInstance and isAssignableFrom
        // to guarantee we return a compatible value. 
        if(object instanceof TemplateNumberModel) {
            Number number = ((TemplateNumberModel)object).getAsNumber();
            if(requiredType.isInstance(number)) {
                return number;
            }
        }
        if(object instanceof TemplateDateModel) {
            Date date = ((TemplateDateModel)object).getAsDate();
            if(requiredType.isInstance(date)) {
                return date;
            }
        }
        if(object instanceof TemplateScalarModel && 
                requiredType.isAssignableFrom(STRING_CLASS)) {
            return ((TemplateScalarModel)object).getAsString();
        }
        if(object instanceof TemplateBooleanModel && 
                requiredType.isAssignableFrom(BOOLEAN_CLASS)) {
            return ((TemplateBooleanModel)object).getAsBoolean() 
            ? Boolean.TRUE : Boolean.FALSE;
        }
        if(object instanceof TemplateHashModel && requiredType.isAssignableFrom(
                HASHADAPTER_CLASS)) {
            return new HashAdapter((TemplateHashModel)object, this);
        }
        if(object instanceof TemplateSequenceModel 
                && requiredType.isAssignableFrom(SEQUENCEADAPTER_CLASS)) {
            return new SequenceAdapter((TemplateSequenceModel)object, this);
        }
        if(object instanceof TemplateCollectionModel && 
                requiredType.isAssignableFrom(SETADAPTER_CLASS)) {
            return new SetAdapter((TemplateCollectionModel)object, this);
        }

        // Last ditch effort - is maybe the model itself instance of the 
        // required type?
        if(requiredType.isInstance(object)) {
            return object;
        }
        
        return CAN_NOT_UNWRAP;
    }
    
    private static Number convertUnwrappedNumber(Class<?> hint, Number number)
    {
        if(hint == Integer.TYPE || hint == Integer.class) {
            return number.intValue();
        }
        if(hint == Long.TYPE || hint == Long.class) {
            return number.longValue();
        }
        if(hint == Float.TYPE || hint == Float.class) {
            return number.floatValue();
        }
        if(hint == Double.TYPE || hint == Double.class) {
            return number.doubleValue();
        }
        if(hint == Byte.TYPE || hint == Byte.class) {
            return number.byteValue();
        }
        if(hint == Short.TYPE || hint == Short.class) {
            return number.shortValue();
        }
        if(hint == BigInteger.class) {
            return number instanceof BigInteger ? number : 
                new BigInteger(number.toString());
        }
        if(hint == BigDecimal.class) {
            if(number instanceof BigDecimal) {
                return number;
            }
            if(number instanceof BigInteger) {
                return new BigDecimal((BigInteger)number);
            }
            if(number instanceof Long) {
                // Because we can't represent long accurately as a 
                // double
                return new BigDecimal(number.toString());
            }
            return new BigDecimal(number.doubleValue());
        }
        // Handle nonstandard Number subclasses as well as directly 
        // java.lang.Number too
        if(hint.isInstance(number)) {
            return number;
        }
        return null;
    }
    
    /**
     * Invokes the specified method, wrapping the return value. The specialty
     * of this method is that if the return value is null, and the return type
     * of the invoked method is void, {@link Constants#NOTHING} is returned.
     * @param object the object to invoke the method on
     * @param method the method to invoke 
     * @param args the arguments to the method
     * @return the wrapped return value of the method.
     * @throws InvocationTargetException if the invoked method threw an exception
     * @throws IllegalAccessException if the method can't be invoked due to an
     * access restriction. 
     * @throws TemplateModelException if the return value couldn't be wrapped
     * (this can happen if the wrapper has an outer identity or is subclassed,
     * and the outer identity or the subclass throws an exception. Plain
     * BeansWrapper never throws TemplateModelException).
     */
    Object invokeMethod(Object object, Method method, Object[] args)
    throws InvocationTargetException, IllegalAccessException
    {
        Object retval = method.invoke(object, args);
        return 
            method.getReturnType() == Void.TYPE
            // We're returning TemplateModel.NOTHING for convenience of 
            // template authors who want to invoke a method for its side effect
            // i.e. ${session.invalidate()}. Returning null would be more
            // intuitive (as return value of a void method is undefined), but
            // this way we don't force people to write an additional ! operator
            // i.e. ${session.invalidate()!}
            ? Constants.NOTHING 
            : _wrap(retval); 
    }

    public Object newInstance(Class<?> clazz, List<TemplateModel> arguments)
    {
        try
        {
            introspectClass(clazz);
            Map<String,Object> classInfo = classCache.get(clazz);
            Object ctors = classInfo.get(CONSTRUCTORS);
            if(ctors == null)
            {
                throw new TemplateModelException("Class " + clazz.getName() + 
                        " has no public constructors.");
            }
            Constructor<?> ctor = null;
            Object[] objargs;
            if(ctors instanceof SimpleMemberModel)
            {
                SimpleMemberModel<Constructor<?>> smm = (SimpleMemberModel<Constructor<?>>)ctors;
                ctor = smm.getMember();
                objargs = smm.unwrapArguments(arguments, this);
            }
            else if(ctors instanceof MethodMap)
            {
                MethodMap<Constructor> methodMap = (MethodMap<Constructor>)ctors; 
                MemberAndArguments<Constructor> maa = 
                    methodMap.getMemberAndArguments(arguments);
                objargs = maa.getArgs();
                ctor = maa.getMember();
            }
            else
            {
                // Cannot happen
                throw new Error();
            }
            return ctor.newInstance(objargs);
        }
        catch (TemplateModelException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TemplateModelException(
                    "Could not create instance of class " + clazz.getName(), e);
        }
    }
    
    void introspectClass(Class<?> clazz)
    {
        if(!classCache.containsKey(clazz))
        {
            synchronized(classCache)
            {
                if(!classCache.containsKey(clazz))
                {
                    introspectClassInternal(clazz);
                }
            }
        }
    }

    private void introspectClassInternal(Class<?> clazz)
    {
        String className = clazz.getName();
        if(cachedClassNames.contains(className))
        {
            assert false;
            if(logger.isInfoEnabled())
            {
                logger.info("Detected a reloaded class [" + className + 
                        "]. Clearing BeansWrapper caches.");
            }
            // Class reload detected, throw away caches
            classCache.clear();
            cachedClassNames = new HashSet<String>();
        }
        classCache.put(clazz, populateClassMap(clazz));
        cachedClassNames.add(className);
    }

    Map<String,Object> getClassKeyMap(Class clazz)
    {
        Map<String, Object> map = classCache.get(clazz);
        if(map == null)
        {
            synchronized(classCache)
            {
                map = classCache.get(clazz);
                if(map == null)
                {
                    introspectClassInternal(clazz);
                    map = classCache.get(clazz);
                }
            }
        }
        return map;
    }

    /**
     * Returns the number of introspected methods/properties that should
     * be available via the TemplateHashModel interface. Affected by the
     * {@link #setMethodsShadowItems(boolean)} and {@link
     * #setExposureLevel(int)} settings.
     */
    int keyCount(Class clazz)
    {
        Map map = getClassKeyMap(clazz);
        int count = map.size();
        if (map.containsKey(CONSTRUCTORS))
            count--;
        if (map.containsKey(GENERIC_GET_KEY))
            count--;
        if (map.containsKey(ARGTYPES))
            count--;
        return count;
    }

    /**
     * Returns the Set of names of introspected methods/properties that
     * should be available via the TemplateHashModel interface. Affected
     * by the {@link #setMethodsShadowItems(boolean)} and {@link
     * #setExposureLevel(int)} settings.
     */
    Set keySet(Class clazz)
    {
        Set set = new HashSet(getClassKeyMap(clazz).keySet());
        set.remove(CONSTRUCTORS);
        set.remove(GENERIC_GET_KEY);
        set.remove(ARGTYPES);
        return set;
    }
    
    /**
     * Populates a map with property and method descriptors for a specified
     * class. If any property or method descriptors specifies a read method
     * that is not accessible, replaces it with appropriate accessible method
     * from a superclass or interface.
     */
    private Map populateClassMap(Class clazz)
    {
        // Populate first from bean info
        Map<Object, Object> map = populateClassMapWithBeanInfo(clazz);
        // Next add constructors
        try
        {
            Constructor[] ctors = clazz.getConstructors();
            if(ctors.length == 1)
            {
                Constructor ctor = ctors[0];
                map.put(CONSTRUCTORS, new SimpleMemberModel<Constructor>(ctor, ctor.getParameterTypes()));
            }
            else if(ctors.length > 1)
            {
                MethodMap<Constructor> ctorMap = new MethodMap<Constructor>("<init>", this);
                for (int i = 0; i < ctors.length; i++)
                {
                    ctorMap.addMember(ctors[i]);
                }
                map.put(CONSTRUCTORS, ctorMap);
            }
        }
        catch(SecurityException e)
        {
            logger.warn("Canont discover constructors for class " + 
                    clazz.getName(), e);
        }
        switch(map.size())
        {
            case 0:
            {
                map = Collections.EMPTY_MAP;
                break; 
            }
            case 1:
            {
                Map.Entry e = map.entrySet().iterator().next();
                map = Collections.singletonMap(e.getKey(), e.getValue());
                break;
            }
        }
        return map;
    }

    private static Class[] componentizeLastArg(Class[] args, boolean varArg) {
        if(varArg && args != null) {
            int lastArg = args.length - 1;
            if(lastArg >= 0) {
                args[lastArg] = args[lastArg].getComponentType();
            }
        }
        return args;
    }
    private Map<Object, Object> populateClassMapWithBeanInfo(Class clazz)
    {
        Map<Object, Object> classMap = new HashMap<Object, Object>();
        Map<MethodSignature, List<Method>> accessibleMethods = discoverAccessibleMethods(clazz);
        Method genericGet = getFirstAccessibleMethod(MethodSignature.GET_STRING_SIGNATURE, accessibleMethods);
        if(genericGet == null) {
            genericGet = getFirstAccessibleMethod(MethodSignature.GET_OBJECT_SIGNATURE, accessibleMethods);
        }
        if(genericGet != null) {
            classMap.put(GENERIC_GET_KEY, genericGet);
        }
        if(exposureLevel == EXPOSE_NOTHING) {
            return classMap;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] pda = beanInfo.getPropertyDescriptors();
            MethodDescriptor[] mda = beanInfo.getMethodDescriptors();

            for(int i = pda.length - 1; i >= 0; --i) {
                PropertyDescriptor pd = pda[i];
                if(pd instanceof IndexedPropertyDescriptor) {
                    IndexedPropertyDescriptor ipd = 
                        (IndexedPropertyDescriptor)pd;
                    Method indexedReadMethod = ipd.getIndexedReadMethod();
                    Method publicIndexedReadMethod = getAccessibleMethod(
                            indexedReadMethod, accessibleMethods);
                    if(publicIndexedReadMethod != null && isSafeMethod(publicIndexedReadMethod)) {
                        try {
                            if(indexedReadMethod != publicIndexedReadMethod) {
                                ipd = new IndexedPropertyDescriptor(
                                        ipd.getName(), ipd.getReadMethod(), 
                                        ipd.getWriteMethod(), publicIndexedReadMethod, 
                                        ipd.getIndexedWriteMethod());
                            }
                            classMap.put(ipd.getName(), ipd);
                            getArgTypes(classMap).put(publicIndexedReadMethod, 
                                    componentizeLastArg(
                                            publicIndexedReadMethod.getParameterTypes(),
                                            publicIndexedReadMethod.isVarArgs()));
                        }
                        catch(IntrospectionException e) {
                            logger.warn("Failed creating a publicly-accessible " +
                                    "property descriptor for " + clazz.getName() + 
                                    " indexed property " + pd.getName() + 
                                    ", read method " + publicIndexedReadMethod + 
                                    ", write method " + ipd.getIndexedWriteMethod(), 
                                    e);
                        }
                    }
                }
                else {
                    Method readMethod = pd.getReadMethod();
                    Method publicReadMethod = getAccessibleMethod(readMethod, accessibleMethods);
                    if(publicReadMethod != null && isSafeMethod(publicReadMethod)) {
                        try {
                            if(readMethod != publicReadMethod) {
                                pd = new PropertyDescriptor(pd.getName(), 
                                        publicReadMethod, pd.getWriteMethod());
                                pd.setReadMethod(publicReadMethod);
                            }
                            classMap.put(pd.getName(), pd);
                        }
                        catch(IntrospectionException e) {
                            logger.warn("Failed creating a publicly-accessible " +
                                    "property descriptor for " + clazz.getName() + 
                                    " property " + pd.getName() + ", read method " + 
                                    publicReadMethod + ", write method " + 
                                    pd.getWriteMethod(), e);
                        }
                    }
                }
            }
            if(exposureLevel < EXPOSE_PROPERTIES_ONLY)
            {
                for(int i = mda.length - 1; i >= 0; --i)
                {
                    MethodDescriptor md = mda[i];
                    Method method = md.getMethod();
                    Method publicMethod = getAccessibleMethod(method, accessibleMethods);
                    if(publicMethod != null && isSafeMethod(publicMethod))
                    {
                        String name = md.getName();
                        Object previous = classMap.get(name);
                        if(previous instanceof Method)
                        {
                            // Overloaded method - replace method with a method map
                            MethodMap<Method> methodMap = new MethodMap<Method>(name, this);
                            methodMap.addMember((Method)previous);
                            methodMap.addMember(publicMethod);
                            classMap.put(name, methodMap);
                            // remove parameter type information
                            getArgTypes(classMap).remove(previous);
                        }
                        else if(previous instanceof MethodMap)
                        {
                            // Already overloaded method - add new overload
                            ((MethodMap<Method>)previous).addMember(publicMethod);
                        }
                        else
                        {
                            // Simple method (this far)
                            classMap.put(name, publicMethod);
                            getArgTypes(classMap).put(publicMethod, 
                                    componentizeLastArg(
                                            publicMethod.getParameterTypes(),
                                            publicMethod.isVarArgs()));
                        }
                    }
                }
            }
            return classMap;
        }
        catch(IntrospectionException e)
        {
            logger.warn("Couldn't properly perform introspection for class " + 
                    clazz, e);
            return new HashMap<Object, Object>();
        }
    }

    private static Map<AccessibleObject, Class<?>[]> getArgTypes(Map classMap) {
        Map<AccessibleObject, Class<?>[]> argTypes = (Map<AccessibleObject, Class<?>[]>)classMap.get(ARGTYPES);
        if(argTypes == null) {
            argTypes = new HashMap<AccessibleObject, Class<?>[]>();
            classMap.put(ARGTYPES, argTypes);
        }
        return argTypes;
    }
    
    static Class[] getArgTypes(Map classMap, AccessibleObject methodOrCtor) {
        return ((Map<AccessibleObject, Class[]>)classMap.get(ARGTYPES)).get(methodOrCtor);
    }

    private static Method getFirstAccessibleMethod(MethodSignature sig, Map<MethodSignature, List<Method>> accessibles)
    {
        List<Method> l = accessibles.get(sig);
        if(l == null || l.isEmpty()) {
            return null;
        }
        return l.iterator().next();
    }

    private static Method getAccessibleMethod(Method m, Map<MethodSignature, List<Method>> accessibles)
    {
        if(m == null) {
            return null;
        }
        MethodSignature sig = new MethodSignature(m);
        List<Method> l = accessibles.get(sig);
        if(l == null) {
            return null;
        }
        for (Method am : l) {
            if(am.getReturnType() == m.getReturnType()) {
                return am;
            }
        }
        return null;
    }
    
    boolean isSafeMethod(Method method)
    {
        return exposureLevel < EXPOSE_SAFE || !UNSAFE_METHODS.contains(method);
    }
    
    /**
     * Retrieves mapping of methods to accessible methods for a class.
     * In case the class is not public, retrieves methods with same 
     * signature as its public methods from public superclasses and 
     * interfaces (if they exist). Basically upcasts every method to the 
     * nearest accessible method.
     */
    private static Map<MethodSignature, List<Method>> discoverAccessibleMethods(Class<?> clazz)
    {
        Map<MethodSignature, List<Method>> map = new HashMap<>();
        discoverAccessibleMethods(clazz, map);
        return map;
    }
    
    private static void discoverAccessibleMethods(Class<?> clazz, Map<MethodSignature, List<Method>> map)
    {
        if(Modifier.isPublic(clazz.getModifiers())) {
            try {
                for(Method method : clazz.getMethods()) {
//                    if (method.isDefault()) System.err.println("KILROY!!! " + clazz.getSimpleName() + ":" + method.getName());
                    MethodSignature sig = new MethodSignature(method);
                    // Contrary to intuition, a class can actually have several 
                    // different methods with same signature *but* different
                    // return types. These can't be constructed using Java the
                    // language, as this is illegal on source code level, but 
                    // the compiler can emit synthetic methods as part of 
                    // generic type reification that will have same signature 
                    // yet different return type than an existing explicitly
                    // declared method. Consider:
                    // public interface I<T> { T m(); }
                    // public class C implements I<Integer> { Integer m() { return 42; } }
                    // C.class will have both "Object m()" and "Integer m()" methods.
                    List<Method> methodList = map.get(sig);
                    if(methodList == null) {
                        methodList = new LinkedList<Method>();
                        map.put(sig, methodList);
                    }
                    methodList.add(method);
                }
                return;
            }
            catch(SecurityException e)
            {
                logger.warn("Could not discover accessible methods of class " + 
                        clazz.getName() + 
                        ", attemping superclasses/interfaces.", e);
                // Fall through and attempt to discover superclass/interface 
                // methods
            }
        }
        for (Class<?> inter : clazz.getInterfaces()) {
            discoverAccessibleMethods(inter, map);
        }
        if(clazz.getSuperclass() != null) {
            discoverAccessibleMethods(clazz.getSuperclass(), map);
        }
    }

    private static final class MethodSignature
    {
        private static final MethodSignature GET_STRING_SIGNATURE = 
            new MethodSignature("get", new Class[] { STRING_CLASS });
        private static final MethodSignature GET_OBJECT_SIGNATURE = 
            new MethodSignature("get", new Class[] { OBJECT_CLASS });

        private final String name;
        private final Class<?>[] args;
        
        private MethodSignature(String name, Class<?>[] args)
        {
            this.name = name;
            this.args = args;
        }
        
        MethodSignature(Method method)
        {
            this(method.getName(), method.getParameterTypes());
        }
        
        public boolean equals(Object o)
        {
            if(o instanceof MethodSignature)
            {
                MethodSignature ms = (MethodSignature)o;
                return ms.name.equals(name) && Arrays.equals(args, ms.args);
            }
            return false;
        }
        
        public int hashCode()
        {
            return name.hashCode() ^ args.length;
        }
    }
    
    private static final Set<Method> createUnsafeMethodsSet()
    {
        Properties props = new Properties();
        InputStream in = ObjectWrapper.class.getResourceAsStream("unsafeMethods.txt");
        if(in != null)
        {
            String methodSpec = null;
            try
            {
                try
                {
                    props.load(in);
                }
                finally
                {
                    in.close();
                }
                Set<Method> set = new HashSet<Method>(props.size() * 4/3, .75f);
                Map<String,Class<?>> primClasses = createPrimitiveClassesMap();
                for (Iterator<Object> iterator = props.keySet().iterator(); iterator.hasNext();)
                {
                    methodSpec = (String) iterator.next();
                    try {
                        set.add(parseMethodSpec(methodSpec, primClasses));
                    }
                    catch(ClassNotFoundException e) {
                        if(DEVELOPMENT) {
                            throw e;
                        }
                    }
                    catch(NoSuchMethodException e) {
                        if(DEVELOPMENT) {
                            throw e;
                        }
                    }
                }
                return set;
            }
            catch(Exception e)
            {
                throw new RuntimeException("Could not load unsafe method " + methodSpec + " " + e.getClass().getName() + " " + e.getMessage());
            }
        }
        return Collections.emptySet();
    }
                                                                           
    private static Method parseMethodSpec(String methodSpec, Map<String,Class<?>> primClasses)
    throws
        ClassNotFoundException,
        NoSuchMethodException
    {
        int brace = methodSpec.indexOf('(');
        int dot = methodSpec.lastIndexOf('.', brace);
        Class<?> clazz = Class.forName(methodSpec.substring(0, dot));
        String methodName = methodSpec.substring(dot + 1, brace);
        String argSpec = methodSpec.substring(brace + 1, methodSpec.length() - 1);
        StringTokenizer tok = new StringTokenizer(argSpec, ",");
        int argcount = tok.countTokens();
        Class<?>[] argTypes = new Class<?>[argcount];
        for (int i = 0; i < argcount; i++)
        {
            String argClassName = tok.nextToken();
            argTypes[i] = (Class<?>)primClasses.get(argClassName);
            if(argTypes[i] == null)
            {
                argTypes[i] = Class.forName(argClassName);
            }
        }
        return clazz.getMethod(methodName, argTypes);
    }

    private static Map<String,Class<?>> createPrimitiveClassesMap()
    {
        Map<String, Class<?>> map = new HashMap<>();
        map.put("boolean", Boolean.TYPE);
        map.put("byte", Byte.TYPE);
        map.put("char", Character.TYPE);
        map.put("short", Short.TYPE);
        map.put("int", Integer.TYPE);
        map.put("long", Long.TYPE);
        map.put("float", Float.TYPE);
        map.put("double", Double.TYPE);
        return map;
    }


    /**
     * Converts any {@link BigDecimal}s in the passed array to the type of
     * the corresponding formal argument of the method.
     */
    public static void coerceBigDecimals(Class<?>[] formalTypes, Object[] args)
    {
        int typeLen = formalTypes.length;
        int argsLen = args.length;
        int min = Math.min(typeLen, argsLen);
        for(int i = 0; i < min; ++i) {
            Object arg = args[i];
            if(arg instanceof BigDecimal) {
                args[i] = coerceBigDecimal((BigDecimal)arg, formalTypes[i]);
            }
        }
        if(argsLen > typeLen) {
            Class<?> varArgType = formalTypes[typeLen - 1];
            for(int i = typeLen; i < argsLen; ++i) {
                Object arg = args[i];
                if(arg instanceof BigDecimal) {
                    args[i] = coerceBigDecimal((BigDecimal)arg, varArgType);
                }
            }
        }
    }
    
    public static Object coerceBigDecimal(BigDecimal bd, Class<?> formalType) {
        // int is expected in most situations, so we check it first
        if(formalType == Integer.TYPE || formalType == Integer.class) {
            return Integer.valueOf(bd.intValue());
        }
        else if(formalType == Double.TYPE || formalType == Double.class) {
            return bd.doubleValue();
        }
        else if(formalType == Long.TYPE || formalType == Long.class) {
            return bd.longValue();
        }
        else if(formalType == Float.TYPE || formalType == Float.class) {
            return bd.floatValue();
        }
        else if(formalType == Short.TYPE || formalType == Short.class) {
            return bd.shortValue();
        }
        else if(formalType == Byte.TYPE || formalType == Byte.class) {
            return bd.byteValue();
        }
        else if(BIGINTEGER_CLASS.isAssignableFrom(formalType)) {
            return bd.toBigInteger();
        }
        return bd;
    }
}