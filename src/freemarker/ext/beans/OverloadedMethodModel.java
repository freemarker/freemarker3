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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

/**
 * A class that will wrap a reflected method call into a
 * {@link freemarker.template.TemplateMethodModel} interface. 
 * It is used by {@link BeanModel} to wrap reflected method calls
 * for overloaded methods.
 * @author Attila Szegedi, szegedia at users dot sourceforge dot net
 * @version $Id: OverloadedMethodModel.java,v 1.25 2005/06/11 12:12:04 szegedia Exp $
 */
class OverloadedMethodModel
implements
	TemplateMethodModelEx,
	TemplateSequenceModel
{
    private final Object object;
    private final MethodMap<Method> methodMap;
    
    public OverloadedMethodModel(Object object, MethodMap<Method> methodMap)
    {
        this.object = object;
        this.methodMap = methodMap;
    }

    /**
     * Invokes the method, passing it the arguments from the list. The actual
     * method to call from several overloaded methods will be chosen based
     * on the classes of the arguments.
     * @throws TemplateModelException if the method cannot be chosen
     * unambiguously.
     */
    public Object exec(List arguments)
    throws
        TemplateModelException
    {
        MemberAndArguments<Method> maa = methodMap.getMemberAndArguments(arguments);
        Method method = maa.getMember();
        try {
            return methodMap.getWrapper().invokeMethod(object, method, maa.getArgs());
        }
        catch(Exception e)
        {
            while(e instanceof InvocationTargetException)
            {
                Throwable t = ((InvocationTargetException)e).getTargetException();
                if(t instanceof Exception)
                {
                    e = (Exception)t;
                }
                else
                {
                    break;
                }
            }
            if((method.getModifiers() & Modifier.STATIC) != 0)
            {
                throw new TemplateModelException("Method " + method + 
                        " threw an exception", e);
            }
            else
            {
                StringBuilder buf = new StringBuilder();
                Object[] args = maa.getArgs();
                for (Object arg : args)
                {
                    buf.append(arg == null ? "null" : arg.getClass().getName()).append(',');
                }
                throw new TemplateModelException("Method " + method + 
                        " threw an exception when invoked on " + object + 
                        " with arguments of types [" + buf + "]", e);
            }
        }
    }

    public TemplateModel get(int index) throws TemplateModelException
    {
        return (TemplateModel) exec(Collections.singletonList(new 
                SimpleNumber(Integer.valueOf(index))));
    }

    public int size() throws TemplateModelException
    {
        throw new TemplateModelException("?size is unsupported for: " + 
                getClass().getName());
    }
}
