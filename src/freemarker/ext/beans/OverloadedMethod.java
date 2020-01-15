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

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * @author Attila Szegedi
 * @version $Id: $
 * @param <T>
 */
abstract class OverloadedMethod<T extends Member> {
    static final Object NO_SUCH_METHOD = new Object();
    static final Object AMBIGUOUS_METHOD = new Object();
    static final Object[] EMPTY_ARGS = new Object[0];

    private Class<?>[][] marshalTypes;
    private final Map<ClassString<T>, Object> selectorCache = 
        new ConcurrentHashMap<ClassString<T>, Object>();
    private final List<T> members = new LinkedList<T>();
    private final Map<T, Class<?>[]> signatures = new HashMap<T, Class<?>[]>();
    
    void addMember(T member) {
        members.add(member);

        Class<?>[] argTypes = OverloadedMethodUtilities.getParameterTypes(member);
        int l = argTypes.length;
        onAddSignature(member, argTypes);
        if(marshalTypes == null) {
            marshalTypes = new Class[l + 1][];
            marshalTypes[l] = argTypes;
            updateSignature(l);
        }
        else if(marshalTypes.length <= l) {
            Class<?>[][] newMarshalTypes = new Class[l + 1][];
            System.arraycopy(marshalTypes, 0, newMarshalTypes, 0, marshalTypes.length);
            marshalTypes = newMarshalTypes;
            marshalTypes[l] = argTypes;
            updateSignature(l);
        }
        else {
            Class<?>[] oldTypes = marshalTypes[l]; 
            if(oldTypes == null) {
                marshalTypes[l] = argTypes;
            }
            else {
                for(int i = 0; i < oldTypes.length; ++i) {
                    oldTypes[i] = OverloadedMethodUtilities.getMostSpecificCommonType(oldTypes[i], argTypes[i]);
                }
            }
            updateSignature(l);
        }

        afterSignatureAdded(l);
    }
    
    Class<?>[] getSignature(T member) {
        Class<?>[] signature = signatures.get(member);
        if(signature == null) {
            signatures.put(member, signature = OverloadedMethodUtilities.getParameterTypes(member));
        }
        return signature;
    }
    
    Class<?>[][] getMarshalTypes() {
	return marshalTypes;
    }
    
    Object getMemberForArgs(Object[] args, boolean varArg) {
	ClassString<T> argTypes = new ClassString<T>(args);
        Object objMember = selectorCache.get(argTypes);
        if(objMember == null) {
            objMember = argTypes.getMostSpecific(members, varArg);
            selectorCache.put(argTypes, objMember);
        }
	return objMember;
    }

    abstract void onAddSignature(T member, Class<?>[] argTypes);
    abstract void updateSignature(int l);
    abstract void afterSignatureAdded(int l);
    
    abstract Object getMemberAndArguments(List<TemplateModel> arguments, 
            BeansWrapper w) throws TemplateModelException;
}
