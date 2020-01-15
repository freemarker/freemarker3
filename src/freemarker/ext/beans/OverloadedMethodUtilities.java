/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package freemarker.ext.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class OverloadedMethodUtilities
{
    static final Class<Object> OBJECT_CLASS = Object.class;
    
    static Class<?> getMostSpecificCommonType(Class<?> c1, Class<?> c2) {
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
        Set<Class<?>> a1 = getAssignables(c1, c2);
        Set<Class<?>> a2 = getAssignables(c2, c1);
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
        List<Class<?>> max = new ArrayList<Class<?>>();
outer:  for (Class<?> clazz : a1) {
            for (Iterator<Class<?>> maxiter = max.iterator(); maxiter.hasNext();) {
                Class<?> maxClazz = maxiter.next();
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

    /**
     * Determines whether a type represented by a class object is 
     * convertible to another type represented by a class object using a 
     * method invocation conversion, without matching object and primitive
     * types. This method is used to determine the more specific type when
     * comparing signatures of methods.
     * @return true if either formal type is assignable from actual type, 
     * or formal and actual are both primitive types and actual can be
     * subject to widening conversion to formal.
     */
    static boolean isMoreSpecific(Class<?> specific, Class<?> generic) {
        // Check for identity or widening reference conversion
        if(generic.isAssignableFrom(specific)) {
            return true;
        }
        // Check for widening primitive conversion.
        if(generic.isPrimitive()) {
            if(generic == Short.TYPE && (specific == Byte.TYPE)) {
                return true;
            }
            if(generic == Integer.TYPE && 
               (specific == Short.TYPE || specific == Byte.TYPE)) {
                return true;
            }
            if(generic == Long.TYPE && 
               (specific == Integer.TYPE || specific == Short.TYPE || 
                specific == Byte.TYPE)) {
                return true;
            }
            if(generic == Float.TYPE && 
               (specific == Long.TYPE || specific == Integer.TYPE || 
                specific == Short.TYPE || specific == Byte.TYPE)) {
                return true;
            }
            if(generic == Double.TYPE && 
               (specific == Float.TYPE || specific == Long.TYPE || 
                specific == Integer.TYPE || specific == Short.TYPE || 
                specific == Byte.TYPE)) {
                return true; 
            }
        }
        return false;
    }
    
    private static Set<Class<?>> getAssignables(Class<?> c1, Class<?> c2) {
        Set<Class<?>> s = new HashSet<Class<?>>();
        collectAssignables(c1, c2, s);
        return s;
    }
    
    private static void collectAssignables(Class<?> c1, Class<?> c2, Set<Class<?>> s) {
        if(c1.isAssignableFrom(c2)) {
            s.add(c1);
        }
        Class<?> sc = c1.getSuperclass();
        if(sc != null) {
            collectAssignables(sc, c2, s);
        }
        Class<?>[] itf = c1.getInterfaces();
        for(int i = 0; i < itf.length; ++i) {
            collectAssignables(itf[i], c2, s);
        }
    }

    static Class<?>[] getParameterTypes(Member member) {
        if(member instanceof Method) {
            return ((Method)member).getParameterTypes();
        }
        if(member instanceof Constructor) {
            return ((Constructor<?>)member).getParameterTypes();
        }
        throw new AssertionError();
    }
}
