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

package freemarker.core.builtins;

import java.util.List;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import freemarker.template.utility.ClassUtil;

/**
 * Implementation of ?new built-in 
 */

public class newBI extends ExpressionEvaluatingBuiltIn {

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

    @Override
    public boolean isSideEffectFree() {
        return false;
    }

    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) throws TemplateException {
        try {
            String classString = ((TemplateScalarModel) model).getAsString();
            return new ConstructorFunction(classString, env);
        } catch (ClassCastException cce) {
            throw new TemplateModelException("Expecting string on left of ?new built-in");

        } catch (NullPointerException npe) {
            throw new InvalidReferenceException("undefined string on left of ?new built-in", env);
        }
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
            ObjectWrapper ow = null;
            if (env != null) ow = env.getObjectWrapper();
            BeansWrapper bw = 
                ow instanceof BeansWrapper 
                ? (BeansWrapper)ow
                        : BeansWrapper.getDefaultInstance();
                return bw.newInstance(cl, arguments);
        }
    }
}
