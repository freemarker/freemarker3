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
