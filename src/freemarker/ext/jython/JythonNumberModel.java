package freemarker.ext.jython;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;

import freemarker.ext.util.ModelFactory;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

/**
 * Model for Jython numeric objects ({@link org.python.core.PyInteger}, {@link org.python.core.PyLong},
 * {@link org.python.core.PyFloat}).
 * @version $Id: JythonNumberModel.java,v 1.10 2003/11/12 21:53:40 ddekany Exp $
 * @author Attila Szegedi
 */
public class JythonNumberModel
extends
    JythonModel
implements
    TemplateNumberModel
{
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object, ObjectWrapper wrapper)
            {
                return new JythonNumberModel((PyObject)object, (JythonWrapper)wrapper);
            }
        };
        
    public JythonNumberModel(PyObject object, JythonWrapper wrapper)
    {
        super(object, wrapper);
    }

    /**
     * Returns either {@link PyObject#__tojava__(java.lang.Class)} with
     * {@link java.lang.Number}.class as argument. If that fails, returns 
     * {@link PyObject#__float__()}.
     */
    public Number getAsNumber() throws TemplateModelException
    {
        try
        {
            Object value = object.__tojava__(java.lang.Number.class);
            if(value == null || value == Py.NoConversion)
            {
                return new Double(object.__float__().getValue());
            }
            return (Number)value;
        }
        catch(PyException e)
        {
            throw new TemplateModelException(e);
        }
    }
}
