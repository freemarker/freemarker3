package freemarker.core.variables;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.beans.*;
import java.io.InputStream;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import freemarker.log.Logger;
import freemarker.core.Scope;

public class ObjectWrapper {
    private static final Class<java.math.BigInteger> BIGINTEGER_CLASS = java.math.BigInteger.class;
    private static final Class<Object> OBJECT_CLASS = Object.class;
    private static final Class<String> STRING_CLASS = String.class;
    public static final Object CAN_NOT_UNWRAP = new Object();

    // When this property is true, some things are stricter. This is mostly to
    // catch anomalous things in development that can otherwise be valid situations
    // for our users.
    private static final boolean DEVELOPMENT = "true".equals(System.getProperty("freemarker.development"));

    private static final Logger logger = Logger.getLogger("freemarker.beans");

    private static final Set<Method> UNSAFE_METHODS = createUnsafeMethodsSet();

    static final Object GENERIC_GET_KEY = new Object();
    private static final Object CONSTRUCTORS = new Object();
    // private static final Object ARGTYPES = new Object();
    private static final Map<AccessibleObject, Class<?>[]> ARGTYPES = new HashMap<>();

    // Cache of hash maps that contain already discovered properties and methods
    // for a specified class. Each key is a Class, each value is a hash map. In
    // that hash map, each key is a property/method name, each value is a
    // MethodDescriptor or a PropertyDescriptor assigned to that property/method.
    private static Map<Class<?>, Map<Object, Object>> classCache = new ConcurrentHashMap<>();
    private static Set<String> cachedClassNames = new HashSet<String>();

    private static int defaultDateType = WrappedDate.UNKNOWN;

    private ObjectWrapper() {
    }

    public static boolean isMap(Object obj) {
        if (obj instanceof Pojo) {
            obj = ((Pojo) obj).getWrappedObject();
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
        ObjectWrapper.defaultDateType = defaultDateType;
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
                || object instanceof Pojo
                || object instanceof Scope
                || object instanceof Boolean
                || object instanceof Number
                || object instanceof String
                || object instanceof Iterator
                || object instanceof Enumeration) {
            return object;
        }
        if (object instanceof CharSequence) {
            return object.toString(); // REVISIT
        }
        if (object instanceof List) {
            // return object;
            return new Pojo(object);
        }
        if (object.getClass().isArray()) {
            return new Pojo(object);
        }
        if (object instanceof Map) {
            return object;
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
        if (object instanceof Pojo) {
            return ((Pojo) object).getWrappedObject();
        }
        return object;
    }

    public static Object unwrap(Object object, Class<?> desiredType) {
        if (object == null) {
            return null;
        }
        object = unwrap(object);
        if (desiredType.isInstance(object)) {
            return object;
        }
        if (desiredType == String.class) {
            return object.toString();
        }
        if (desiredType == Boolean.TYPE || desiredType == Boolean.class) {
            if (object instanceof Boolean) {
                return (Boolean) object;
            }
            if (object instanceof WrappedBoolean) {
                return ((WrappedBoolean) object).getAsBoolean() ? Boolean.TRUE : Boolean.FALSE;
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
        }
        if (desiredType == Date.class && object instanceof WrappedDate) {
            // REVISIT
            return ((WrappedDate) object).getAsDate();
        }
        return CAN_NOT_UNWRAP;
    }

    private static Object[] unwrapParams(Object[] params, Class<?>[] desiredTypes) {
        if (params == null || params.length == 0) {
            return new Object[0];
        }
        Object[] result = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            result[i] = unwrap(params[i], desiredTypes[i]);
        }
        return result;
    }

    /**
     * Invokes the specified method, wrapping the return value. The specialty
     * of this method is that if the return value is null, and the return type
     * of the invoked method is void, {@link Constants#NOTHING} is returned.
     * 
     * @param object the object to invoke the method on
     * @param method the method to invoke
     * @param args   the arguments to the method
     * @return the wrapped return value of the method.
     * @throws InvocationTargetException if the invoked method threw an exception
     * @throws IllegalAccessException    if the method can't be invoked due to an
     *                                   access restriction.
     * @throws EvaluationException       if the return value couldn't be wrapped
     *                                   (this can happen if the wrapper has an
     *                                   outer identity or is subclassed,
     *                                   and the outer identity or the subclass
     *                                   throws an exception. Plain
     *                                   BeansWrapper never throws
     *                                   EvaluationException).
     */
    static Object invokeMethod(Object object, Method method, Object[] args)
            throws InvocationTargetException, IllegalAccessException {
        Class<?>[] types = method.getParameterTypes();
        assert args == null && types.length == 0 || args.length == types.length;
        Object[] params = unwrapParams(args, types);
        assert params.length == types.length;
        Object retval = method.invoke(object, params);
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

    public static Object newInstance(Class<?> clazz, List<Object> arguments) {
        try {
            introspectClass(clazz);
            Map<Object, Object> classInfo = classCache.get(clazz);
            Object ctors = classInfo.get(CONSTRUCTORS);
            if (ctors == null) {
                throw new EvaluationException("Class " + clazz.getName() +
                        " has no public constructors.");
            }
            Constructor<?> ctor = null;
            Object[] objargs;
            if (ctors instanceof SimpleMemberModel) {
                SimpleMemberModel<Constructor<?>> smm = (SimpleMemberModel<Constructor<?>>) ctors;
                ctor = smm.getMember();
                objargs = smm.unwrapArguments(arguments);
            } else if (ctors instanceof MethodMap) {
                MethodMap<Constructor> methodMap = (MethodMap<Constructor>) ctors;
                MemberAndArguments<Constructor> maa = methodMap.getMemberAndArguments(arguments);
                objargs = maa.getArgs();
                ctor = maa.getMember();
            } else {
                // Cannot happen
                throw new Error();
            }
            return ctor.newInstance(objargs);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(
                    "Could not create instance of class " + clazz.getName(), e);
        }
    }

    static void introspectClass(Class<?> clazz) {
        if (!classCache.containsKey(clazz)) {
            synchronized (classCache) {
                if (!classCache.containsKey(clazz)) {
                    introspectClassInternal(clazz);
                }
            }
        }
    }

    private static void introspectClassInternal(Class<?> clazz) {
        String className = clazz.getName();
        if (cachedClassNames.contains(className)) {
            assert false;
            if (logger.isInfoEnabled()) {
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

    static Map<Object, Object> getClassKeyMap(Class<?> clazz) {
        Map<Object, Object> map = classCache.get(clazz);
        if (map == null) {
            synchronized (classCache) {
                map = classCache.get(clazz);
                if (map == null) {
                    introspectClassInternal(clazz);
                    map = classCache.get(clazz);
                }
            }
        }
        return map;
    }

    /**
     * Populates a map with property and method descriptors for a specified
     * class. If any property or method descriptors specifies a read method
     * that is not accessible, replaces it with appropriate accessible method
     * from a superclass or interface.
     */
    private static Map<Object, Object> populateClassMap(Class<?> clazz) {
        // Populate first from bean info
        Map<Object, Object> map = populateClassMapWithBeanInfo(clazz);
        // Next add constructors
        try {
            Constructor[] ctors = clazz.getConstructors();
            if (ctors.length == 1) {
                Constructor ctor = ctors[0];
                map.put(CONSTRUCTORS, new SimpleMemberModel<Constructor>(ctor, ctor.getParameterTypes()));
            } else if (ctors.length > 1) {
                MethodMap<Constructor> ctorMap = new MethodMap<Constructor>("<init>");
                for (int i = 0; i < ctors.length; i++) {
                    ctorMap.addMember(ctors[i]);
                }
                map.put(CONSTRUCTORS, ctorMap);
            }
        } catch (SecurityException e) {
            logger.warn("Cannot discover constructors for class " +
                    clazz.getName(), e);
        }
        switch (map.size()) {
            case 0: {
                map = Collections.EMPTY_MAP;
                break;
            }
            case 1: {
                Map.Entry e = map.entrySet().iterator().next();
                map = Collections.singletonMap(e.getKey(), e.getValue());
                break;
            }
        }
        return map;
    }

    private static Class<?>[] componentizeLastArg(Class<?>[] args, boolean varArg) {
        if (varArg && args != null) {
            int lastArg = args.length - 1;
            if (lastArg >= 0) {
                args[lastArg] = args[lastArg].getComponentType();
            }
        }
        return args;
    }

    private static Map<Object, Object> populateClassMapWithBeanInfo(Class<?> clazz) {
        Map<Object, Object> classMap = new HashMap<>();
        Map<MethodSignature, List<Method>> accessibleMethods = discoverAccessibleMethods(clazz);
        Method genericGet = getFirstAccessibleMethod(MethodSignature.GET_STRING_SIGNATURE, accessibleMethods);
        if (genericGet == null) {
            genericGet = getFirstAccessibleMethod(MethodSignature.GET_OBJECT_SIGNATURE, accessibleMethods);
        }
        if (genericGet != null) {
            classMap.put(GENERIC_GET_KEY, genericGet);
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] pda = beanInfo.getPropertyDescriptors();
            MethodDescriptor[] mda = beanInfo.getMethodDescriptors();

            for (int i = pda.length - 1; i >= 0; --i) {
                PropertyDescriptor pd = pda[i];
                if (pd instanceof IndexedPropertyDescriptor) {
                    IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
                    Method indexedReadMethod = ipd.getIndexedReadMethod();
                    Method publicIndexedReadMethod = getAccessibleMethod(
                            indexedReadMethod, accessibleMethods);
                    if (publicIndexedReadMethod != null && isSafeMethod(publicIndexedReadMethod)) {
                        try {
                            if (indexedReadMethod != publicIndexedReadMethod) {
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
                        } catch (IntrospectionException e) {
                            logger.warn("Failed creating a publicly-accessible " +
                                    "property descriptor for " + clazz.getName() +
                                    " indexed property " + pd.getName() +
                                    ", read method " + publicIndexedReadMethod +
                                    ", write method " + ipd.getIndexedWriteMethod(),
                                    e);
                        }
                    }
                } else {
                    Method readMethod = pd.getReadMethod();
                    Method publicReadMethod = getAccessibleMethod(readMethod, accessibleMethods);
                    if (publicReadMethod != null && isSafeMethod(publicReadMethod)) {
                        try {
                            if (readMethod != publicReadMethod) {
                                pd = new PropertyDescriptor(pd.getName(),
                                        publicReadMethod, pd.getWriteMethod());
                                pd.setReadMethod(publicReadMethod);
                            }
                            classMap.put(pd.getName(), pd);
                        } catch (IntrospectionException e) {
                            logger.warn("Failed creating a publicly-accessible " +
                                    "property descriptor for " + clazz.getName() +
                                    " property " + pd.getName() + ", read method " +
                                    publicReadMethod + ", write method " +
                                    pd.getWriteMethod(), e);
                        }
                    }
                }
            }
            for (int i = mda.length - 1; i >= 0; --i) {
                MethodDescriptor md = mda[i];
                Method method = md.getMethod();
                Method publicMethod = getAccessibleMethod(method, accessibleMethods);
                if (publicMethod != null && isSafeMethod(publicMethod)) {
                    String name = md.getName();
                    Object previous = classMap.get(name);
                    if (previous instanceof Method) {
                        // Overloaded method - replace method with a method map
                        MethodMap<Method> methodMap = new MethodMap<Method>(name);
                        methodMap.addMember((Method) previous);
                        methodMap.addMember(publicMethod);
                        classMap.put(name, methodMap);
                        // remove parameter type information
                        getArgTypes(classMap).remove(previous);
                    } else if (previous instanceof MethodMap) {
                        // Already overloaded method - add new overload
                        ((MethodMap<Method>) previous).addMember(publicMethod);
                    } else {
                        // Simple method (this far)
                        classMap.put(name, publicMethod);
                        getArgTypes(classMap).put(publicMethod,
                                componentizeLastArg(
                                        publicMethod.getParameterTypes(),
                                        publicMethod.isVarArgs()));
                    }
                }
            }
            return classMap;
        } catch (IntrospectionException e) {
            logger.warn("Couldn't properly perform introspection for class " +
                    clazz, e);
            return new HashMap<Object, Object>();
        }
    }

    private static Map<AccessibleObject, Class<?>[]> getArgTypes(Map classMap) {
        Map<AccessibleObject, Class<?>[]> argTypes = (Map<AccessibleObject, Class<?>[]>) classMap.get(ARGTYPES);
        if (argTypes == null) {
            argTypes = new HashMap<AccessibleObject, Class<?>[]>();
            classMap.put(ARGTYPES, argTypes);
        }
        return argTypes;
    }

    static Class[] getArgTypes(Map classMap, AccessibleObject methodOrCtor) {
        return ((Map<AccessibleObject, Class[]>) classMap.get(ARGTYPES)).get(methodOrCtor);
    }

    private static Method getFirstAccessibleMethod(MethodSignature sig,
            Map<MethodSignature, List<Method>> accessibles) {
        List<Method> l = accessibles.get(sig);
        if (l == null || l.isEmpty()) {
            return null;
        }
        return l.iterator().next();
    }

    private static Method getAccessibleMethod(Method m, Map<MethodSignature, List<Method>> accessibles) {
        if (m == null) {
            return null;
        }
        MethodSignature sig = new MethodSignature(m);
        List<Method> l = accessibles.get(sig);
        if (l == null) {
            return null;
        }
        for (Method am : l) {
            if (am.getReturnType() == m.getReturnType()) {
                return am;
            }
        }
        return null;
    }

    private static boolean isSafeMethod(Method method) {
        return !UNSAFE_METHODS.contains(method);
    }

    /**
     * Retrieves mapping of methods to accessible methods for a class.
     * In case the class is not public, retrieves methods with same
     * signature as its public methods from public superclasses and
     * interfaces (if they exist). Basically upcasts every method to the
     * nearest accessible method.
     */
    private static Map<MethodSignature, List<Method>> discoverAccessibleMethods(Class<?> clazz) {
        Map<MethodSignature, List<Method>> map = new HashMap<>();
        discoverAccessibleMethods(clazz, map);
        return map;
    }

    private static void discoverAccessibleMethods(Class<?> clazz, Map<MethodSignature, List<Method>> map) {
        if (Modifier.isPublic(clazz.getModifiers())) {
            try {
                for (Method method : clazz.getMethods()) {
                    // if (method.isDefault()) System.err.println("KILROY!!! " +
                    // clazz.getSimpleName() + ":" + method.getName());
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
                    if (methodList == null) {
                        methodList = new LinkedList<Method>();
                        map.put(sig, methodList);
                    }
                    methodList.add(method);
                }
                return;
            } catch (SecurityException e) {
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
        if (clazz.getSuperclass() != null) {
            discoverAccessibleMethods(clazz.getSuperclass(), map);
        }
    }

    private static final class MethodSignature {
        private static final MethodSignature GET_STRING_SIGNATURE = new MethodSignature("get",
                new Class[] { STRING_CLASS });
        private static final MethodSignature GET_OBJECT_SIGNATURE = new MethodSignature("get",
                new Class[] { OBJECT_CLASS });

        private final String name;
        private final Class<?>[] args;

        private MethodSignature(String name, Class<?>[] args) {
            this.name = name;
            this.args = args;
        }

        MethodSignature(Method method) {
            this(method.getName(), method.getParameterTypes());
        }

        public boolean equals(Object o) {
            if (o instanceof MethodSignature) {
                MethodSignature ms = (MethodSignature) o;
                return ms.name.equals(name) && Arrays.equals(args, ms.args);
            }
            return false;
        }

        public int hashCode() {
            return name.hashCode() ^ args.length;
        }
    }

    private static final Set<Method> createUnsafeMethodsSet() {
        Properties props = new Properties();
        InputStream in = ObjectWrapper.class.getResourceAsStream("unsafeMethods.txt");
        if (in != null) {
            String methodSpec = null;
            try {
                try {
                    props.load(in);
                } finally {
                    in.close();
                }
                Set<Method> set = new HashSet<Method>(props.size() * 4 / 3, .75f);
                Map<String, Class<?>> primClasses = createPrimitiveClassesMap();
                for (Iterator<Object> iterator = props.keySet().iterator(); iterator.hasNext();) {
                    methodSpec = (String) iterator.next();
                    try {
                        set.add(parseMethodSpec(methodSpec, primClasses));
                    } catch (ClassNotFoundException e) {
                        if (DEVELOPMENT) {
                            throw e;
                        }
                    } catch (NoSuchMethodException e) {
                        if (DEVELOPMENT) {
                            throw e;
                        }
                    }
                }
                return set;
            } catch (Exception e) {
                throw new RuntimeException("Could not load unsafe method " + methodSpec + " " + e.getClass().getName()
                        + " " + e.getMessage());
            }
        }
        return Collections.emptySet();
    }

    private static Method parseMethodSpec(String methodSpec, Map<String, Class<?>> primClasses)
            throws ClassNotFoundException,
            NoSuchMethodException {
        int brace = methodSpec.indexOf('(');
        int dot = methodSpec.lastIndexOf('.', brace);
        Class<?> clazz = Class.forName(methodSpec.substring(0, dot));
        String methodName = methodSpec.substring(dot + 1, brace);
        String argSpec = methodSpec.substring(brace + 1, methodSpec.length() - 1);
        StringTokenizer tok = new StringTokenizer(argSpec, ",");
        int argcount = tok.countTokens();
        Class<?>[] argTypes = new Class<?>[argcount];
        for (int i = 0; i < argcount; i++) {
            String argClassName = tok.nextToken();
            argTypes[i] = (Class<?>) primClasses.get(argClassName);
            if (argTypes[i] == null) {
                argTypes[i] = Class.forName(argClassName);
            }
        }
        return clazz.getMethod(methodName, argTypes);
    }

    private static Map<String, Class<?>> createPrimitiveClassesMap() {
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
    static void coerceBigDecimals(Class<?>[] formalTypes, Object[] args) {
        int typeLen = formalTypes.length;
        int argsLen = args.length;
        int min = Math.min(typeLen, argsLen);
        for (int i = 0; i < min; ++i) {
            Object arg = args[i];
            if (arg instanceof BigDecimal) {
                args[i] = coerceBigDecimal((BigDecimal) arg, formalTypes[i]);
            }
        }
        if (argsLen > typeLen) {
            Class<?> varArgType = formalTypes[typeLen - 1];
            for (int i = typeLen; i < argsLen; ++i) {
                Object arg = args[i];
                if (arg instanceof BigDecimal) {
                    args[i] = coerceBigDecimal((BigDecimal) arg, varArgType);
                }
            }
        }
    }

    private static Object coerceBigDecimal(BigDecimal bd, Class<?> formalType) {
        // int is expected in most situations, so we check it first
        if (formalType == Integer.TYPE || formalType == Integer.class) {
            return bd.intValue();
        } else if (formalType == Double.TYPE || formalType == Double.class) {
            return bd.doubleValue();
        } else if (formalType == Long.TYPE || formalType == Long.class) {
            return bd.longValue();
        } else if (formalType == Float.TYPE || formalType == Float.class) {
            return bd.floatValue();
        } else if (formalType == Short.TYPE || formalType == Short.class) {
            return bd.shortValue();
        } else if (formalType == Byte.TYPE || formalType == Byte.class) {
            return bd.byteValue();
        } else if (BIGINTEGER_CLASS.isAssignableFrom(formalType)) {
            return bd.toBigInteger();
        }
        return bd;
    }
}