package freemarker.ext.jython;

import org.python.core.PyException;
import org.python.core.PyObject;

import freemarker.ext.util.ModelFactory;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Model for Jython dictionaries ({@link org.python.core.PyDictionary}
 * and {@link org.python.core.PyStringMap}).
 * Note that the basic {@link JythonModel} already provides access to the
 * {@link PyObject#__finditem__(String)} method. This class only adds 
 * {@link TemplateHashModelEx} functionality in a somewhat skewed way. One
 * could say it even violates TemplateHashModelEx semantics, as both the
 * returned keys and values are only those from the item mapping, while the
 * <code>get()</code> method works for attributes as well. However, in practice
 * when you ask for <code>dict?keys</code> inside a template, you'll really
 * want to retrieve only items, not attributes so this is considered OK.
 * @version $Id: JythonHashModel.java,v 1.14 2003/11/12 21:53:40 ddekany Exp $
 * @author Attila Szegedi
 */
public class JythonHashModel
extends 
    JythonModel 
implements 
    TemplateHashModelEx
{
    private static final String KEYS = "keys";
    private static final String KEYSET = "keySet";
    private static final String VALUES = "values";
    
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object, ObjectWrapper wrapper)
            {
                return new JythonHashModel((PyObject)object, (JythonWrapper)wrapper);
            }
        };
        
    public JythonHashModel(PyObject object, JythonWrapper wrapper)
    {
        super(object, wrapper);
    }
    
    /**
     * Returns {@link PyObject#__len__()}.
     */
    public int size() throws TemplateModelException
    {
        try
        {
            return object.__len__();
        }
        catch(PyException e)
        {
            throw new TemplateModelException(e);
        }
    }

    /**
     * Returns either <code>object.__findattr__("keys").__call__()</code>
     * or <code>object.__findattr__("keySet").__call__()</code>.
     */
    public TemplateCollectionModel keys() throws TemplateModelException
    {
        try
        {
            PyObject method = object.__findattr__(KEYS);
            if(method == null)
            {
                method = object.__findattr__(KEYSET);
            }
            if(method != null)
            {
                return (TemplateCollectionModel)wrapper.wrap(method.__call__());
            }
        }
        catch(PyException e)
        {
            throw new TemplateModelException(e);
        }
        throw new TemplateModelException("'?keys' is not supported as there is no 'keys' nor 'keySet' attribute on an instance of " + object.getType());
    }

    /**
     * Returns <code>object.__findattr__("values").__call__()</code>.
     */
    public TemplateCollectionModel values() throws TemplateModelException
    {
        try
        {
            PyObject method = object.__findattr__(VALUES);
            if(method != null)
            {
                return (TemplateCollectionModel)wrapper.wrap(method.__call__());
            }
        }
        catch(PyException e)
        {
            throw new TemplateModelException(e);
        }
        throw new TemplateModelException("'?values' is not supported as there is no 'values' attribute on an instance of " + object.getType());
    }
}
