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
