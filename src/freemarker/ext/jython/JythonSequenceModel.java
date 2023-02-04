package freemarker.ext.jython;

import org.python.core.PyException;
import org.python.core.PyObject;

import freemarker.ext.util.ModelFactory;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateSequenceModel;

/**
 * Model for Jython sequence objects ({@link org.python.core.PySequence} descendants).
 * @version $Id: JythonSequenceModel.java,v 1.13 2003/11/12 21:53:40 ddekany Exp $
 * @author Attila Szegedi
 */
public class JythonSequenceModel
extends
    JythonModel
implements 
    TemplateSequenceModel,
    TemplateCollectionModel
{
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object, ObjectWrapper wrapper)
            {
                return new JythonSequenceModel((PyObject)object, (JythonWrapper)wrapper);
            }
        };
        
    public JythonSequenceModel(PyObject object, JythonWrapper wrapper)
    {
        super(object, wrapper);
    }

    /**
     * Returns {@link PyObject#__finditem__(int)}.
     */
    public TemplateModel get(int index) throws TemplateModelException
    {
        try
        {
            return wrapper.wrap(object.__finditem__(index));
        }
        catch(PyException e)
        {
            throw new TemplateModelException(e);
        }
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

    public TemplateModelIterator iterator()
    {
        return new TemplateModelIterator()
        {
            int i = 0;
            
            public boolean hasNext() throws TemplateModelException
            {
                return i < size();
            }

            public TemplateModel next() throws TemplateModelException
            {
                return get(i++);
            }
        };
    }
}
