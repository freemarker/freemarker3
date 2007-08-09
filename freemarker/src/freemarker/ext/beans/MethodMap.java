/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.ext.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

class MethodMap<T extends Member>
{
    private static final Class<BigDecimal> BIGDECIMAL_CLASS = BigDecimal.class;
    private static final Class<Number> NUMBER_CLASS = Number.class;
    
    private static final Object[] EMPTY_ARGS = new Object[0];
    private static final Class<Object> OBJECT_CLASS = java.lang.Object.class;
    private static final ClassString EMPTY_STRING = new ClassString(EMPTY_ARGS);    
    
    private static final Object NO_SUCH_METHOD = new Object();
    private static final Object AMBIGUOUS_METHOD = new Object();
    
    private final String name;
    // Cache of Class[] --> AccessibleObject. Maps the actual types involved in
    // a method/constructor call to the most specific method/constructor for 
    // those types. Speeds up subsequent invocations.
    private final Map<ClassString, Object> selectorCache = 
        new HashMap<ClassString, Object>();
    private Map<T, Class[]> cachedArgumentTypes; 
    private UnwrapTypes[] unwrapTypes;
    private final List<T> methods = new LinkedList<T>();
    
    MethodMap(String name)
    {
        this.name = name;
    }
    
    void addMember(T member)
    {
        methods.add(member);
        boolean isVarArg;
        Class[] argTypes;
        if(member instanceof Method) {
            Method method = (Method)member;
            isVarArg = method.isVarArgs();
            argTypes = method.getParameterTypes();
        }
        else if(member instanceof Constructor) {
            Constructor ctor = (Constructor)member;
            isVarArg = ctor.isVarArgs();
            argTypes = ctor.getParameterTypes();
        }
        else {
            throw new AssertionError();
        }
        if(cachedArgumentTypes == null) {
            cachedArgumentTypes = new HashMap<T, Class[]>();
        }
        Class[] cachedArgTypes = argTypes.clone();
        if(isVarArg) {
            componentizeLastArg(cachedArgTypes); 
        }
        cachedArgumentTypes.put(member, cachedArgTypes);
        updateUnwrapTypes(argTypes, isVarArg);
    }
    
    private void updateUnwrapTypes(Class[] argTypes, boolean varArg)
    {
        int l = argTypes.length - 1;
        if(l == -1) {
            return;
        }
        if(varArg) {
            componentizeLastArg(argTypes);
        }
        if(unwrapTypes == null) {
            unwrapTypes = new UnwrapTypes[l + 1];
            unwrapTypes[l] = new UnwrapTypes(argTypes, varArg);
            updateFromSurroundingVarArg(l);
        }
        else if(unwrapTypes.length <= l) {
            UnwrapTypes[] newUnwrapTypes = new UnwrapTypes[l + 1];
            System.arraycopy(unwrapTypes, 0, newUnwrapTypes, 0, unwrapTypes.length);
            unwrapTypes = newUnwrapTypes;
            unwrapTypes[l] = new UnwrapTypes(argTypes, varArg);
            updateFromSurroundingVarArg(l);
        }
        else {
            UnwrapTypes oldTypes = unwrapTypes[l]; 
            if(oldTypes == null) {
                unwrapTypes[l] = new UnwrapTypes(argTypes, varArg);
                updateFromSurroundingVarArg(l);
            }
            else {
                oldTypes.update(argTypes, varArg);
            }
        }
        if(varArg) {
            // If this unwrap spec is vararg, it influences the types in all
            // unwrap specs longer than itself.
            UnwrapTypes newTypes = unwrapTypes[l];
            for(int i = l + 1; i < unwrapTypes.length; ++i) {
                UnwrapTypes existingTypes = unwrapTypes[i];
                if(existingTypes != null) {
                    existingTypes.update(newTypes);
                }
            }
            // It also influences the types in the unwrap spec that is exactly
            // one argument shorter (as vararg methods can be invoked with 0
            // variable arguments, that is, with k-1 cardinality).
            if(l > 0) {
                UnwrapTypes oneShorterTypes = unwrapTypes[l - 1];
                if(oneShorterTypes != null) {
                    oneShorterTypes.update(newTypes);
                }
            }
        }
    }
    
    private void updateFromSurroundingVarArg(int l) {
        UnwrapTypes newTypes = unwrapTypes[l];
        // First vararg unwrap type spec with less parameters than the 
        // current spec influences the types of the current unwrap spec.
        for(int i = l; i-->0;) {
            UnwrapTypes previousTypes = unwrapTypes[i];
            if(previousTypes != null && previousTypes.isVarArg()) {
                newTypes.update(previousTypes);
                break;
            }
        }
        // Vararg unwrap spec with exactly one parameter more than the current
        // spec influences the types of the current spec
        if(l + 1 < unwrapTypes.length) {
            UnwrapTypes oneLongerTypes = unwrapTypes[l + 1];
            if(oneLongerTypes != null && oneLongerTypes.isVarArg()) {
                newTypes.update(oneLongerTypes);
            }
        }
    }
    
    MemberAndArguments<T> getMemberAndArguments(List<TemplateModel> arguments, 
            BeansWrapper wrapper) throws TemplateModelException {
        Object[] args = wrapper.unwrapArguments(arguments, getUnwrapTypes(arguments));
        T member = getMostSpecific(args);
        if(args != null) {
            //TODO: coerceBigDecimals should deal with vararg
            Class[] memberArgTypes = cachedArgumentTypes.get(member);
            BeansWrapper.coerceBigDecimals(memberArgTypes, args);                
            if(isVarArgs(member)) {
                args = BeansWrapper.packVarArgs(args, memberArgTypes);
            }
        }
        return new MemberAndArguments<T>(member, args);
    }
    
    private static boolean isVarArgs(Member member) {
        if(member instanceof Method) { 
            return ((Method)member).isVarArgs();
        }
        if(member instanceof Constructor) {
            return ((Constructor)member).isVarArgs();
        }
        throw new AssertionError();
    }
    
    private Class[] getUnwrapTypes(List args) throws TemplateModelException
    {
        int l = args.size() - 1;
        if(l == -1) {
            return EMPTY_STRING.getClasses();
        }
        return getUnwrapTypes(l).getUnwrapTypes();
    }
    
    private UnwrapTypes getUnwrapTypes(int l) throws TemplateModelException {
        if(l < unwrapTypes.length) {
            UnwrapTypes retval = unwrapTypes[l];
            if(retval != null) {
                return retval;
            }
        }
        else {
            l = unwrapTypes.length;
        }
        while(--l >= 0) {
            UnwrapTypes retval = unwrapTypes[l];
            if(retval != null && retval.isVarArg()) {
                return retval;
            }
        }
        throw new TemplateModelException("No signature of method " + 
                name + " accepts " + (l + 1) + " arguments");
    }
    
    private T getMostSpecific(Object[] args) throws TemplateModelException
    {
        ClassString<T> cs = args == null ? EMPTY_STRING : new ClassString<T>(args);
        synchronized(selectorCache) {
            Object obj = selectorCache.get(cs);
            if(obj == null) {
                selectorCache.put(cs, obj = cs.getMostSpecific(methods, false));
            }
            if(obj instanceof Member) {
                return (T)obj;
            }
            if(obj == NO_SUCH_METHOD) {
                selectorCache.put(cs, obj = cs.getMostSpecific(methods, true));
                if(obj instanceof Member) {
                    return (T)obj;
                }
                if(obj == NO_SUCH_METHOD) {
                    throw new TemplateModelException("No signature of method " + 
                            name + " matches " + cs.listArgumentTypes());
                }
            }
            // Can be only AMBIGUOUS_METHOD
            throw new TemplateModelException(
                    "Multiple signatures of method " + name + " match " + 
                    cs.listArgumentTypes());
        }
    }
    
    private static void componentizeLastArg(Class[] c) {
        int cl = c.length - 1;
        if(cl >= 0) {
            c[cl] = c[cl].getComponentType();
        }
    }
    
    private static final class ClassString<T extends Member>
    {
        private final Class[] classes;
        
        ClassString(Object[] objects)
        {
            int l = objects.length;
            classes = new Class[l];
            for(int i = 0; i < l; ++i)
            {
                Object obj = objects[i];
                classes[i] = obj == null ? OBJECT_CLASS : obj.getClass();
            }
        }
        
        Class[] getClasses()
        {
            return classes;
        }
        
        public int hashCode()
        {
            int hash = 0;
            for(int i = 0; i < classes.length; ++i) {
                hash ^= classes[i].hashCode();
            }
            return hash;
        }
        
        public boolean equals(Object o)
        {
            if(o instanceof ClassString) {
                ClassString cs = (ClassString)o;
                if(cs.classes.length != classes.length) {
                    return false;
                }
                for(int i = 0; i < classes.length; ++i) {
                    if(cs.classes[i] != classes[i]) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        
        private static final int MORE_SPECIFIC = 0;
        private static final int LESS_SPECIFIC = 1;
        private static final int INDETERMINATE = 2;
        
        Object getMostSpecific(List<T> methods, boolean varArg)
        {
            LinkedList<T> applicables = getApplicables(methods, varArg);
            if(applicables.isEmpty()) {
                return NO_SUCH_METHOD;
            }
            if(applicables.size() == 1) {
                return applicables.getFirst();
            }
            LinkedList<T> maximals = new LinkedList<T>();
            for (T applicable : applicables)
            {
                Class[] appArgs = getParameterTypes(applicable);
                if(varArg) {
                    componentizeLastArg(appArgs);
                }
                boolean lessSpecific = false;
                for (Iterator<T> maximal = maximals.iterator(); 
                    maximal.hasNext();)
                {
                    Member max = maximal.next();
                    Class[] maxArgs = getParameterTypes(max);
                    if(varArg) {
                        componentizeLastArg(maxArgs);
                    }
                    switch(moreSpecific(appArgs, maxArgs, varArg)) {
                        case MORE_SPECIFIC: {
                            maximal.remove();
                            break;
                        }
                        case LESS_SPECIFIC: {
                            lessSpecific = true;
                            break;
                        }
                    }
                }
                if(!lessSpecific) {
                    maximals.addLast(applicable);
                }
            }
            if(maximals.size() > 1) {
                // Check whether all methods are override compatible
                
                return AMBIGUOUS_METHOD;
            }
            return maximals.getFirst();
        }
        
        private static Class[] getParameterTypes(Member member)
        {
            if(member instanceof Method) {
                return ((Method)member).getParameterTypes();
            }
            if(member instanceof Constructor) {
                return ((Constructor)member).getParameterTypes();
            }
            throw new AssertionError();
        }
        
        private static int moreSpecific(Class[] c1, Class[] c2, boolean varArg)
        {
            boolean c1MoreSpecific = false;
            boolean c2MoreSpecific = false;
            final int cl1 = c1.length;
            final int cl2 = c2.length;
            int min = Math.min(cl1, cl2);
            for(int i = 0; i < min; ++i) {
                if(c1[i] != c2[i]) {
                    c1MoreSpecific = 
                        c1MoreSpecific ||
                        isMoreSpecific(c1[i], c2[i]);
                    c2MoreSpecific = 
                        c2MoreSpecific ||
                        isMoreSpecific(c2[i], c1[i]);
                }
            }
            if(varArg) {
                int max = Math.max(cl1, cl2);
                for(int i = min; i < max; ++i) {
                    Class class1 = i < cl1 ? c1[i] : c1[cl1 - 1];
                    Class class2 = i < cl2 ? c2[i] : c2[cl2 - 1];
                    if(class1 != class2) {
                        c1MoreSpecific = 
                            c1MoreSpecific ||
                            isMoreSpecific(class1, class2);
                        c2MoreSpecific = 
                            c2MoreSpecific ||
                            isMoreSpecific(class2, class1);
                    }
                }
            }
            else {
                assert cl1 == cl2;
            }
            if(c1MoreSpecific) {
                if(c2MoreSpecific) {
                    return INDETERMINATE;
                }
                return MORE_SPECIFIC;
            }
            if(c2MoreSpecific) {
                return LESS_SPECIFIC;
            }
            return INDETERMINATE;
        }
        
        /**
         * Returns all methods that are applicable to actual
         * parameter classes represented by this ClassString object.
         */
        LinkedList<T> getApplicables(List<T> methods, boolean varArg)
        {
            LinkedList<T> list = new LinkedList<T>();
            for (T member : methods)
            {
                if(isApplicable(member, varArg))
                {
                    list.add(member);
                }
            }
            return list;
        }
        
        /**
         * Returns true if the supplied method is applicable to actual
         * parameter classes represented by this ClassString object.
         * 
         */
        private boolean isApplicable(T member, boolean varArg)
        {
            if(varArg != isVarArgs(member))
            {
                return false;
            }
            final Class[] formalTypes = getParameterTypes(member);
            final int fl = formalTypes.length;
            final int cl = classes.length;
            if(varArg) {
                final int fl1 = fl - 1;
                if(fl1 > cl) { // more fixargs expected than supplied
                    return false;
                }
                // Check fixarg compatibility
                for(int i = 0; i < fl1; ++i) {
                    if(!isMethodInvocationConvertible(formalTypes[i], 
                            classes[i])) {
                        return false;
                    }
                }
                // Check vararg compatibility
                if(fl1 < cl) {
                    Class varArgType = formalTypes[fl1].getComponentType(); 
                    for(int i = fl1; i < cl; ++i) {
                        if(!isMethodInvocationConvertible(varArgType, 
                                classes[fl1])) {
                            return false;
                        }
                    }
                }
            } else {
                if(fl != cl) {
                    return false;
                }
                for(int i = 0; i < cl; ++i) {
                    if(!isMethodInvocationConvertible(formalTypes[i], 
                            classes[i])) {
                        return false;
                    }
                }
            }
            return true;
        }
        
        /**
         * Determines whether a type represented by a class object is
         * convertible to another type represented by a class object using a 
         * method invocation conversion, treating object types of primitive 
         * types as if they were primitive types (that is, a Boolean actual 
         * parameter type matches boolean primitive formal type). This behavior
         * is because this method is used to determine applicable methods for 
         * an actual parameter list, and primitive types are represented by 
         * their object duals in reflective method calls.
         * @param formal the formal parameter type to which the actual 
         * parameter type should be convertible
         * @param actual the actual parameter type.
         * @return true if either formal type is assignable from actual type, 
         * or formal is a primitive type and actual is its corresponding object
         * type or an object type of a primitive type that can be converted to
         * the formal type.
         */
        private static boolean isMethodInvocationConvertible(Class<?> formal, Class<?> actual)
        {
            // Check for identity or widening reference conversion
            if(formal.isAssignableFrom(actual))
            {
                return true;
            }
            // Check for boxing with widening primitive conversion. Note that 
            // actual parameters are never primitives.
            if(formal.isPrimitive())
            {
                if(formal == Boolean.TYPE)
                    return actual == Boolean.class;
                if(formal == Character.TYPE)
                    return actual == Character.class;
                if(formal == Byte.TYPE && actual == Byte.class)
                    return true;
                if(formal == Short.TYPE &&
                   (actual == Short.class || actual == Byte.class))
                    return true;
                if(formal == Integer.TYPE && 
                   (actual == Integer.class || actual == Short.class || 
                    actual == Byte.class))
                    return true;
                if(formal == Long.TYPE && 
                   (actual == Long.class || actual == Integer.class || 
                    actual == Short.class || actual == Byte.class))
                    return true;
                if(formal == Float.TYPE && 
                   (actual == Float.class || actual == Long.class || 
                    actual == Integer.class || actual == Short.class || 
                    actual == Byte.class))
                    return true;
                if(formal == Double.TYPE && 
                   (actual == Double.class || actual == Float.class || 
                    actual == Long.class || actual == Integer.class || 
                    actual == Short.class || actual == Byte.class))
                    return true; 
            }
            // Special case for BigDecimals as we deem BigDecimal to be
            // convertible to any numeric type - either object or primitive.
            // This can actually cause us trouble as this is a narrowing 
            // conversion, not widening. 
            return isBigDecimalConvertible(formal, actual);
        }
        
        private String listArgumentTypes()
        {
            StringBuffer buf = 
                new StringBuffer(classes.length * 32).append('(');
            for(int i = 0; i < classes.length; ++i)
            {
                buf.append(classes[i].getName()).append(',');
            }
            buf.setLength(buf.length() - 1);
            return buf.append(')').toString();
        }
    }

    /**
     * Determines whether a type represented by "specific" class object is 
     * convertible to another type represented by a class object "generic" 
     * using a method invocation conversion, matching object and primitive 
     * types. This method is used to determine the more specific type when 
     * comparing signatures of methods.
     * @return true if either "specific" type is assignable to "generic" type, 
     * or "specific" is primitive, and "generic" is a primitive type or its
     * boxing counterpart such that "specific" can be converted to "generic"'s
     * primitive type using a widening primitive conversion.
     */
    private static boolean isMoreSpecific(Class specific, Class<?> generic) {
        // Check for identity or widening reference conversion
        if(generic.isAssignableFrom(specific)) {
            return true;
        }
        // Check for widening primitive conversion or autoboxing. We use the 
        // same priority that JDK 1.5 javac uses -- primitive types are 
        // considered more specific than their boxed counterparts.
        if(specific.isPrimitive()) {
            if(specific == Boolean.TYPE) {
                return generic == Boolean.class;
            }
            if(specific == Character.TYPE) {
                return generic == Character.class;
            }
            if(specific == Byte.TYPE && ((generic.isPrimitive() && 
                    generic != Boolean.TYPE && generic != Character.TYPE) ||
                        generic == Byte.class || generic == Short.class || 
                        generic == Integer.class || generic == Long.class || 
                        generic == Float.class || generic == Double.class)) {
                return true;
            }
            if(specific == Short.TYPE && ((generic.isPrimitive() && 
                    generic != Boolean.TYPE && generic != Character.TYPE && 
                    generic != Byte.TYPE) || generic == Short.class || 
                    generic == Integer.class || generic == Long.class || 
                    generic == Float.class || generic == Double.class)) {
                return true;
            }
            if(specific == Integer.TYPE && (generic == Long.TYPE || 
                    generic == Float.TYPE || generic == Double.TYPE || 
                    generic == Integer.class || generic == Long.class || 
                    generic == Float.class || generic == Double.class)) {
                return true;
            }
            if(specific == Long.TYPE && (generic == Float.TYPE || 
                    generic == Double.TYPE || generic == Long.class || 
                    generic == Float.class || generic == Double.class)) {
                return true;
            }
            if(specific == Float.TYPE && (generic == Double.TYPE || 
                    generic == Float.class || generic == Double.class)) {
                return true;
            }
            if(specific == Double.TYPE && generic == Double.class) {
                return true;
            }
        }
        return isBigDecimalConvertible(generic, specific);
    }
    
    private static boolean isBigDecimalConvertible(Class formal, Class actual)
    {
        // BigDecimal 
        if(BIGDECIMAL_CLASS.isAssignableFrom(actual))
        {
            if(NUMBER_CLASS.isAssignableFrom(formal))
            {
                return true;
            }
            if(formal.isPrimitive() && 
               formal != Boolean.TYPE && formal != Character.TYPE)
            {
               return true;
            }
        }
        return false;
    }
    
    private static Class getMostSpecificCommonType(Class c1, Class c2)
    {
        if(c1 == c2) {
            return c1;
        }
        if(c2.isPrimitive()) {
            if(c2 == Byte.TYPE) c2 = Byte.class;
            else if(c2 == Short.TYPE) c2 = Short.class;
            else if(c2 == Character.TYPE) c2 = Character.class;
            else if(c2 == Integer.TYPE) c2 = Integer.class;
            else if(c2 == Float.TYPE) c2 = Float.class;
            else if(c2 == Long.TYPE) c2 = Long.class;
            else if(c2 == Double.TYPE) c2 = Double.class;
        }
        Set<Class> a1 = getAssignables(c1, c2);
        Set<Class> a2 = getAssignables(c2, c1);
        a1.retainAll(a2);
        if(a1.isEmpty()) {
            // Can happen when at least one of the arguments is an interface, as
            // they don't have Object at the root of their hierarchy
            return Object.class;
        }
        // Gather maximally specific elements. Yes, there can be more than one 
        // thank to interfaces. I.e., if you call this method for String.class 
        // and Number.class, you'll have Comparable, Serializable, and Object as 
        // maximal elements. 
        List<Class> max = new ArrayList<Class>();
outer:  for (Iterator iter = a1.iterator(); iter.hasNext();) {
            Class clazz = (Class) iter.next();
            for (Iterator<Class> maxiter = max.iterator(); maxiter.hasNext();) {
                Class maxClazz = maxiter.next();
                if(isMoreSpecific(maxClazz, clazz)) {
                    // It can't be maximal, if there's already a more specific
                    // maximal than it.
                    continue outer;
                }
                if(isMoreSpecific(clazz, maxClazz)) {
                    // If it's more specific than a currently maximal element,
                    // that currently maximal is no longer a maximal.
                    maxiter.remove();
                }
            }
            // If we get here, no current maximal is more specific than the
            // current class, so it is considered maximal as well
            max.add(clazz);
        }
        if(max.size() > 1) {
            return OBJECT_CLASS;
        }
        return max.get(0);
    }

    private static Set<Class> getAssignables(Class c1, Class c2)
    {
        Set<Class> s = new HashSet<Class>();
        collectAssignables(c1, c2, s);
        return s;
    }
    
    private static void collectAssignables(Class<?> c1, Class c2, Set<Class> s)
    {
        if(c1.isAssignableFrom(c2)) {
            s.add(c1);
        }
        Class sc = c1.getSuperclass();
        if(sc != null) {
            collectAssignables(sc, c2, s);
        }
        Class[] itf = c1.getInterfaces();
        for(int i = 0; i < itf.length; ++i) {
            collectAssignables(itf[i], c2, s);
        }
    }
    
    private static class UnwrapTypes
    {
        private final Class[] unwrapTypes;
        private boolean varArg;
        
        UnwrapTypes(Class[] argTypes, boolean varArg) {
            this.unwrapTypes = argTypes;
            this.varArg = varArg;
        }
        
        void update(UnwrapTypes other) {
            update(other.unwrapTypes, false);
        }
        
        void update(Class[] argTypes, boolean varArg) {
            if(varArg) {
                this.varArg = true;
            }
            final int ul = unwrapTypes.length;
            final int al = argTypes.length;
            int min = Math.min(al, ul);
            for(int i = 0; i < min; ++i) {
                unwrapTypes[i] = getMostSpecificCommonType(unwrapTypes[i], 
                        argTypes[i]);
            }
            if(ul > al) {
                Class varArgType = argTypes[al - 1];
                for(int i = al; i < ul; ++i) {
                    unwrapTypes[i] = getMostSpecificCommonType(unwrapTypes[i], 
                            varArgType);
                }
            }
        }
        
        Class[] getUnwrapTypes() {
            return unwrapTypes;
        }
        
        boolean isVarArg() {
            return varArg;
        }
    }
}
