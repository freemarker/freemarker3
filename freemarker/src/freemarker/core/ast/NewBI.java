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

package freemarker.core.ast;

import java.util.List;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.ext.beans.*;
import freemarker.template.*;
import freemarker.template.utility.ClassUtil;

/**
 * A built-in that allows us to instantiate an instance of a java class.
 * Usage is something like:
 * &lt;#assign foobar = "foo.bar.MyClass"?new() /&gt;
 */

class NewBI extends BuiltIn
{
    
    static final Class<TemplateModel> TM_CLASS = TemplateModel.class;
    static final Class<freemarker.ext.beans.BeanModel> BEAN_MODEL_CLASS = freemarker.ext.beans.BeanModel.class;
    static Class<?> JYTHON_MODEL_CLASS;
    static {
        try {
            JYTHON_MODEL_CLASS = Class.forName("freemarker.ext.jython.JythonModel");
        } catch (Throwable e) {
            JYTHON_MODEL_CLASS = null;
        }
    }
    
    TemplateModel _getAsTemplateModel(Environment env)
            throws TemplateException 
    {
        TemplateModel tm = target.getAsTemplateModel(env);
        String classname = null;
        try {
            classname = ((TemplateScalarModel) tm).getAsString();
        } 
        catch (ClassCastException cce) {
            invalidTypeException(tm, target, env, "string");
        } 
        catch (NullPointerException npe) {
            throw new InvalidReferenceException(getStartLocation() 
                + "\nCould not resolve expression: " + target, env);
        }
        return new ConstructorFunction(classname, env);
    }

    static class ConstructorFunction implements TemplateMethodModelEx {

        private final Class<?> cl;
        private final Environment env;
        
        public ConstructorFunction(String classname, Environment env) throws TemplateException {
            this.env = env;
            try {
                cl = ClassUtil.forName(classname);
                if (!TM_CLASS.isAssignableFrom(cl)) {
                    throw new TemplateException("Class " + cl.getName() + " does not implement freemarker.template.TemplateModel", env);
                }
                if (BEAN_MODEL_CLASS.isAssignableFrom(cl)) {
                    throw new TemplateException("Bean Models cannot be instantiated using the ?new built-in", env);
                }
                if (JYTHON_MODEL_CLASS != null && JYTHON_MODEL_CLASS.isAssignableFrom(cl)) {
                    throw new TemplateException("Jython Models cannot be instantiated using the ?new built-in", env);
                }
            } 
            catch (ClassNotFoundException cnfe) {
                throw new TemplateException(cnfe, env);
            }
        }

        public Object exec(List arguments) throws TemplateModelException {
            ObjectWrapper ow = env.getObjectWrapper();
            BeansWrapper bw = 
                ow instanceof BeansWrapper 
                ? (BeansWrapper)ow
                : BeansWrapper.getDefaultInstance();
            return bw.newInstance(cl, arguments);
        }
    }
}
