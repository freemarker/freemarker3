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
import java.util.List;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

class MethodMap<T extends Member>
{
    private final String name;
    private final BeansWrapper wrapper;
    private final OverloadedMethod<T> fixArgMethod = new OverloadedFixArgMethod<T>();
    private OverloadedMethod<T> varArgMethod;
    
    MethodMap(String name, BeansWrapper wrapper) {
        this.name = name;
        this.wrapper = wrapper;
    }
    
    BeansWrapper getWrapper() {
        return wrapper;
    }
    
    void addMember(T member) {
        fixArgMethod.addMember(member);
        if(isVarArgs(member)) {
            if(varArgMethod == null) {
                varArgMethod = new OverloadedVarArgMethod<T>();
            }
            varArgMethod.addMember(member);
        }
    }
    
    MemberAndArguments<T> getMemberAndArguments(List<TemplateModel> arguments) 
    throws TemplateModelException {
        Object memberAndArguments = fixArgMethod.getMemberAndArguments(arguments, wrapper);
        if(memberAndArguments == OverloadedMethod.NO_SUCH_METHOD) {
            if(varArgMethod != null) {
                memberAndArguments = varArgMethod.getMemberAndArguments(arguments, wrapper);
            }
            if(memberAndArguments == OverloadedMethod.NO_SUCH_METHOD) {
                throw new TemplateModelException("No signature of method " + 
                        name + " matches the arguments");
            }
        }
        if(memberAndArguments == OverloadedMethod.AMBIGUOUS_METHOD) {
            throw new TemplateModelException("Multiple signatures of method " + 
                    name + " match the arguments");
        }
        return (MemberAndArguments<T>)memberAndArguments;
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
}
