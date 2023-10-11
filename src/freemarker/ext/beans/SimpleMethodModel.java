package freemarker.ext.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Collections;

import freemarker.annotations.Parameters;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

/**
 * A class that will wrap a reflected method call into a
 * {@link freemarker.template.TemplateMethodModel} interface. 
 * It is used by {@link Pojo} to wrap reflected method calls
 * for non-overloaded methods.
 * @author Attila Szegedi, szegedia at users dot sourceforge dot net
 * @version $Id: SimpleMethodModel.java,v 1.27 2005/06/11 12:12:04 szegedia Exp $
 */
public final class SimpleMethodModel extends SimpleMemberModel<Method>
    implements
    TemplateMethodModel,
    TemplateSequenceModel
{
    private final Object object;
    private final ObjectWrapper wrapper;

    /**
     * Creates a model for a specific method on a specific object.
     * @param object the object to call the method on. Can be
     * <tt>null</tt> for static methods.
     * @param method the method that will be invoked.
     */
    SimpleMethodModel(Object object, Method method, Class[] argTypes, 
            ObjectWrapper wrapper)
    {
        super(method, argTypes);
        this.object = object;
        this.wrapper = wrapper;
    }

    /**
     * Invokes the method, passing it the arguments from the list.
     */
    public Object exec(List arguments)
        throws
        TemplateModelException
    {
        try
        {
            return wrapper.invokeMethod(object, getMember(), unwrapArguments(
                    arguments, wrapper));
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
            if((getMember().getModifiers() & Modifier.STATIC) != 0)
            {
                throw new TemplateModelException("Method " + getMember() + 
                        " threw an exception", e);
            }
            else
            {
                throw new TemplateModelException("Method " + getMember() + 
                        " threw an exception when invoked on " + object, e);
            }
        }
    }
    
    public Object get(int index) throws TemplateModelException
    {
        return exec(Collections.singletonList(Integer.valueOf(index)));
    }

    public int size() 
    {
        throw new TemplateModelException("?size is unsupported for: " + getClass().getName());
    }
    
    public Parameters getParametersAnnotation() {
        return getMember().getAnnotation(Parameters.class);
    }
    
    public String toString() {
        return getMember().toGenericString();
    }
}