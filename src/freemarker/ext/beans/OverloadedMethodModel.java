package freemarker.ext.beans;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import freemarker.template.WrappedMethod;
import freemarker.template.EvaluationException;
import freemarker.template.WrappedSequence;

/**
 * A class that will wrap a reflected method call into a
 * {@link freemarker.template.WrappedMethod} interface. 
 * It is used by {@link Pojo} to wrap reflected method calls
 * for overloaded methods.
 * @author Attila Szegedi, szegedia at users dot sourceforge dot net
 * @version $Id: OverloadedMethodModel.java,v 1.25 2005/06/11 12:12:04 szegedia Exp $
 */
class OverloadedMethodModel implements WrappedMethod, WrappedSequence
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
     * @throws EvaluationException if the method cannot be chosen
     * unambiguously.
     */
    public Object exec(List arguments)
    {
        MemberAndArguments<Method> maa = methodMap.getMemberAndArguments(arguments);
        Method method = maa.getMember();
        try {
            return ObjectWrapper.invokeMethod(object, method, maa.getArgs());
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
                throw new EvaluationException("Method " + method + 
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
                throw new EvaluationException("Method " + method + 
                        " threw an exception when invoked on " + object + 
                        " with arguments of types [" + buf + "]", e);
            }
        }
    }

    public Object get(int index) 
    {
        return exec(Collections.singletonList(Integer.valueOf(index)));
    }

    public int size() 
    {
        throw new EvaluationException("?size is unsupported for: " + 
                getClass().getName());
    }
}
