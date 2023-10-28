package freemarker.ext.beans;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.beans.*;
import java.io.InputStream;
import java.lang.reflect.*;
import java.math.BigDecimal;
import freemarker.log.Logger;
import freemarker.template.*;
import freemarker.core.Scope;

public class ObjectWrapper 
{
    private static final Class<java.math.BigInteger> BIGINTEGER_CLASS = java.math.BigInteger.class;
    private static final Class<Object> OBJECT_CLASS = Object.class;
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

    // Cache of hash maps that contain already discovered properties and methods
    // for a specified class. Each key is a Class, each value is a hash map. In
    // that hash map, each key is a property/method name, each value is a
    // MethodDescriptor or a PropertyDescriptor assigned to that property/method.
    private static Map<Class<?>,Map<Object,Object>> classCache = new ConcurrentHashMap<>();
    private static Set<String> cachedClassNames = new HashSet<String>();

    private static int exposureLevel = EXPOSE_SAFE;
    private static boolean methodsShadowItems = true;
    private static int defaultDateType = WrappedDate.UNKNOWN;

    private static boolean strict = false;
    
    /**
     * Creates a new instance of BeansWrapper. The newly created instance
     * will use the null reference as its null object, it will use
     * {@link #EXPOSE_SAFE} method exposure level, and will not cache
     * model instances.
     */
    private ObjectWrapper() {}

    public static boolean isMap(Object obj) {
        if (obj instanceof WrappedHash) {
            return true;
        }
        if (obj instanceof Pojo) {
            obj = ((Pojo)obj).getWrappedObject();
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
            for (int i = 0; i < tsm.size() ; i++) result.add(tsm.get(i));
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
        if (obj instanceof WrappedNumber) {
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
        if (obj instanceof WrappedNumber) {
            return ((WrappedNumber) obj).getAsNumber();
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
        if (obj.getClass().isArray()) return true;
        if (obj instanceof WrappedSequence) return true;
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
        }
        return obj instanceof Iterable;
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
     * @see #setStrict(boolean)
     */
    public static boolean isStrict() {
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
     * <p>If this property is <tt>true</tt> then an attempt to read a bean property in
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
    public static void setStrict(boolean strict) {
    	ObjectWrapper.strict = strict;
    }

    /**
     * Sets the method exposure level. By default, set to <code>EXPOSE_SAFE</code>.
     * @param exposureLevel can be any of the <code>EXPOSE_xxx</code>
     * constants.
     */
    public static void setExposureLevel(int exposureLevel)
    {
        if(exposureLevel < EXPOSE_ALL || exposureLevel > EXPOSE_NOTHING)
        {
            throw new IllegalArgumentException("Illegal exposure level " + exposureLevel);
        }
        ObjectWrapper.exposureLevel = exposureLevel;
    }
    
    static int getExposureLevel()
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
    public static synchronized void setMethodsShadowItems(boolean methodsShadowItems)
    {
        ObjectWrapper.methodsShadowItems = methodsShadowItems;
    }
    
    static boolean isMethodsShadowItems()
    {
        return methodsShadowItems;
    }
    
    /**
     * Sets the default date type to use for date models that result from
     * a plain <tt>java.util.Date</tt> instead of <tt>java.sql.Date</tt> or
     * <tt>java.sql.Time</tt> or <tt>java.sql.Timestamp</tt>. Default value is 
     * {@link WrappedDate#UNKNOWN}.
     * @param defaultDateType the new default date type.
     */
    public static synchronized void setDefaultDateType(int defaultDateType) {
        ObjectWrapper.defaultDateType = defaultDateType;
    }
    
    static synchronized int getDefaultDateType() {
        return defaultDateType;
    }
    
    public static Object wrap(Object object) {
        if(object == null) {
            return Constants.JAVA_NULL;
        }
        if (isMarkedAsPojo(object.getClass())) {
            return new Pojo(object);
        }
        if (object instanceof WrappedVariable) {
            return object;
        }
        if (object instanceof Scope) {
            return object;
        }
        if (object instanceof Boolean 
           || object instanceof Number 
           || object instanceof String 
           || object instanceof Iterator
           || object instanceof Enumeration)
        {
            return object;
        }
        if (object instanceof List) {
            //return new ListModel((List<?>)object);
            //return object;
            return new Pojo(object);
        }
        if (object instanceof Map) {
            return  new SimpleMapModel((Map<?,?>)object);
        }
        if (object.getClass().isArray()) {
            return new ArrayModel(object);
        }
        if (object instanceof Date) {
            return new DateModel((Date) object);
        }
        if (object instanceof ResourceBundle) {
            return new ResourceBundleModel((ResourceBundle)object);
        }
        return new Pojo(object);
    }

    private static Map<Class<?>, Boolean> markedAsPojoLookup = new HashMap<>();

    private static boolean isMarkedAsPojo(Class<?> clazz) {
        Boolean lookupValue = markedAsPojoLookup.get(clazz);
        if (lookupValue != null) return lookupValue;
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
        if (clazz.getSuperclass()!=null) {
            lookupValue = isMarkedAsPojo(clazz.getSuperclass());
        } else {
            lookupValue = false;
        }
        markedAsPojoLookup.put(clazz,lookupValue);
        return lookupValue;
    }

    /**
     * Attempts to unwrap a model into underlying object. Generally, this
     * method is the inverse of the {@link #wrap(Object)} method. In addition
     * it will unwrap arbitrary {@link WrappedNumber} instances into
     * a number, arbitrary {@link WrappedDate} instances into a date,
     * {@link WrappedString} instances into a String, and
     * {@link WrappedBoolean} instances into a Boolean.
     * All other objects are returned unchanged.
     */
    public static Object unwrap(Object object) {
        if(object == null) {
            throw new EvaluationException("invalid reference");
        }
        if (object == Constants.JAVA_NULL) {
            return null;
        }
        if (object instanceof Pojo) {
            return ((Pojo)object).getWrappedObject();
        }
        return object;
        //if (!(object instanceof WrappedVariable)) {
        //    return object;
        //}
        //throw new UnsupportedOperationException("Don't know how to unwrap this object " + object.getClass());
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
     * @throws EvaluationException if the return value couldn't be wrapped
     * (this can happen if the wrapper has an outer identity or is subclassed,
     * and the outer identity or the subclass throws an exception. Plain
     * BeansWrapper never throws EvaluationException).
     */
    static Object invokeMethod(Object object, Method method, Object[] args)
    throws InvocationTargetException, IllegalAccessException
    {
        Object retval = method.invoke(object, args);
        return method.getReturnType() == Void.TYPE ?
            // We're returning WrappedVariable.NOTHING for convenience of 
            // template authors who want to invoke a method for its side effect
            // i.e. ${session.invalidate()}. Returning null would be more
            // intuitive (as return value of a void method is undefined), but
            // this way we don't force people to write an additional ! operator
            // i.e. ${session.invalidate()!}
            Constants.NOTHING 
            : wrap(retval); 
    }

    public static Object newInstance(Class<?> clazz, List<Object> arguments)
    {
        try
        {
            introspectClass(clazz);
            Map<Object,Object> classInfo = classCache.get(clazz);
            Object ctors = classInfo.get(CONSTRUCTORS);
            if(ctors == null)
            {
                throw new EvaluationException("Class " + clazz.getName() + 
                        " has no public constructors.");
            }
            Constructor<?> ctor = null;
            Object[] objargs;
            if(ctors instanceof SimpleMemberModel)
            {
                SimpleMemberModel<Constructor<?>> smm = (SimpleMemberModel<Constructor<?>>)ctors;
                ctor = smm.getMember();
                objargs = smm.unwrapArguments(arguments);
            }
            else if(ctors instanceof MethodMap)
            {
                MethodMap<Constructor> methodMap = (MethodMap<Constructor>)ctors; 
                MemberAndArguments<Constructor> maa = methodMap.getMemberAndArguments(arguments);
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
        catch (EvaluationException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new EvaluationException(
                    "Could not create instance of class " + clazz.getName(), e);
        }
    }
    
    static void introspectClass(Class<?> clazz)
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

    private static void introspectClassInternal(Class<?> clazz)
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

    static Map<Object,Object> getClassKeyMap(Class<?> clazz)
    {
        Map<Object, Object> map = classCache.get(clazz);
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
     * Returns the Set of names of introspected methods/properties that
     * should be available via the WrappedHash interface. Affected
     * by the {@link #setMethodsShadowItems(boolean)} and {@link
     * #setExposureLevel(int)} settings.
     */
    static Set<Object> keySet(Class<?> clazz)
    {
        Set<Object> set = new HashSet<>(getClassKeyMap(clazz).keySet());
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
    private static Map<Object,Object> populateClassMap(Class<?> clazz)
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
                MethodMap<Constructor> ctorMap = new MethodMap<Constructor>("<init>");
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

    private static Class<?>[] componentizeLastArg(Class<?>[] args, boolean varArg) {
        if(varArg && args != null) {
            int lastArg = args.length - 1;
            if(lastArg >= 0) {
                args[lastArg] = args[lastArg].getComponentType();
            }
        }
        return args;
    }
    
    private static Map<Object, Object> populateClassMapWithBeanInfo(Class<?> clazz)
    {
        Map<Object, Object> classMap = new HashMap<>();
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
                            MethodMap<Method> methodMap = new MethodMap<Method>(name);
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
    
    private static boolean isSafeMethod(Method method)
    {
        return exposureLevel < EXPOSE_SAFE || !UNSAFE_METHODS.contains(method);
    }
    
    static public boolean isEmpty(Object model) 
    {
        if (model instanceof Pojo) {
            return ((Pojo) model).isEmpty();
        } else if (model instanceof WrappedSequence) {
            return ((WrappedSequence) model).size() == 0;
        } else if (isString(model)) {
            String s = asString(model);
            return (s == null || s.length() == 0);
        } else if (model instanceof Iterable) {
            return !((Iterable<?>) model).iterator().hasNext();
        } else if (model instanceof WrappedHash) {
            return ((WrappedHash) model).isEmpty();
        } else if (isNumber(model) || (model instanceof WrappedDate) ||
                isBoolean(model)) {
            return false;
        } else {
            return true;
        }
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
    static void coerceBigDecimals(Class<?>[] formalTypes, Object[] args)
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
    
    private static Object coerceBigDecimal(BigDecimal bd, Class<?> formalType) {
        // int is expected in most situations, so we check it first
        if(formalType == Integer.TYPE || formalType == Integer.class) {
            return bd.intValue();
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