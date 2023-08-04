package freemarker.ext.beans;

import java.lang.reflect.Array;

import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

/**
 * <p>A class that will wrap an arbitrary array into {@link TemplateCollectionModel}
 * and {@link TemplateSequenceModel} interfaces. It supports element retrieval through the <tt>array[index]</tt>
 * syntax and can be iterated as a list.
 * @author Attila Szegedi
 * @version $Id: ArrayModel.java,v 1.26 2003/06/03 13:21:32 szegedia Exp $
 */
public class ArrayModel extends BeanModel implements TemplateCollectionModel, TemplateSequenceModel
{
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object)
            {
                return new ArrayModel(object);
            }
        };
        
    // Cached length of the array
    private int length;

    /**
     * Creates a new model that wraps the specified array object.
     * @param array the array object to wrap into a model.
     * @param wrapper the {@link ObjectWrapper} associated with this model.
     * Every model has to have an associated {@link ObjectWrapper} instance. The
     * model gains many attributes from its wrapper, including the caching 
     * behavior, method exposure level, method-over-item shadowing policy etc.
     * @throws IllegalArgumentException if the passed object is not a Java array.
     */
    public ArrayModel(Object array)
    {
        super(array);
        Class clazz = array.getClass();
        if(!clazz.isArray())
            throw new IllegalArgumentException("Object is not an array, it is " + array.getClass().getName());
        length = Array.getLength(array);
    }


    public java.util.Iterator<Object> iterator()
    {
        return new Iterator();
    }

    public Object get(int index)
    throws
        TemplateModelException
    {
        try
        {
            return wrap(Array.get(object, index));
        }
        catch(IndexOutOfBoundsException e)
        {
        	return null; // Allow existence built-ins, see coment in CollectionModel (JR)
//            throw new TemplateModelException("Index out of bounds: " + index);
        }
    }

    private class Iterator
    implements 
        TemplateSequenceModel,
        java.util.Iterator<Object>
    {
        private int position = 0;

        public boolean hasNext()
        {
            return position < length;
        }

        public Object get(int index)
        throws
            TemplateModelException
        {
            return ArrayModel.this.get(index);
        }

        public Object next()
        throws
            TemplateModelException
        {
            return position < length ? get(position++) : null;
        }

        public int size() 
        {
            return ArrayModel.this.size();
        }
    }

    public int size() 
    {
        return length;
    }

    public boolean isEmpty() {
        return length == 0;
    }
}
