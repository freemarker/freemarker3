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

package freemarker.ext.jsp;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.tagext.JspTag;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.StringUtil;

class JspTagModelBase<T extends JspTag>
{
    private final Class tagClass;
    private final Method dynaSetter;
    private final Map<String, Method> propertySetters = new HashMap<String, Method>();
    
    protected JspTagModelBase(Class<? extends T> tagClass) throws IntrospectionException {
        this.tagClass = tagClass;
        BeanInfo bi = Introspector.getBeanInfo(tagClass);
        PropertyDescriptor[] pda = bi.getPropertyDescriptors();
        for (int i = 0; i < pda.length; i++) {
            PropertyDescriptor pd = pda[i];
            Method m = pd.getWriteMethod();
            if(m != null) {
                propertySetters.put(pd.getName(), m);
            }
        }
        // Check to see if the tag implements the JSP2.0 DynamicAttributes
        // interface, to allow setting of arbitrary attributes
        Method dynaSetter;
        try {
            dynaSetter = tagClass.getMethod("setDynamicAttribute",
                            new Class[] {String.class, String.class, Object.class});
        }
        catch (NoSuchMethodException nsme) {
            dynaSetter = null;
        }
        this.dynaSetter = dynaSetter;
    }
    
    T getTagInstance() throws IllegalAccessException, InstantiationException {
        return (T)tagClass.newInstance();
    }
    
    void setupTag(Object tag, Map<String, TemplateModel> args, ObjectWrapper wrapper)
    throws 
        TemplateModelException, 
        InvocationTargetException, 
        IllegalAccessException
    {
        BeansWrapper bwrapper = 
            wrapper instanceof BeansWrapper
            ? (BeansWrapper)wrapper
            : BeansWrapper.getDefaultInstance();
        if(args != null && !args.isEmpty()) {
            Object[] aarg = new Object[1];
            for (Map.Entry<String, TemplateModel> entry: args.entrySet()) {
                Object arg = bwrapper.unwrap(entry.getValue());
                aarg[0] = arg;
                Method m = propertySetters.get(entry.getKey());
                if (m == null) {
                    if (dynaSetter == null) {
                        throw new TemplateModelException("Unknown property "
                                + StringUtil.jQuote(entry.getKey())
                                + " on instance of " + tagClass.getName());
                    }
                    else {
                        dynaSetter.invoke(tag, new Object[] {null, entry.getKey(), aarg[0]});
                    }
                }
                else {
                    if(arg instanceof BigDecimal) {
                        aarg[0] = BeansWrapper.coerceBigDecimal(
                                (BigDecimal)arg, m.getParameterTypes()[0]);
                    }
                    m.invoke(tag, aarg);
                }
            }
        }
    }

}
