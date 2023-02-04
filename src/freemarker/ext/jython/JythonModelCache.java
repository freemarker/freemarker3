package freemarker.ext.jython;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyLong;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyStringMap;

import freemarker.ext.util.ModelCache;
import freemarker.template.TemplateModel;

class JythonModelCache extends ModelCache
{
    private final JythonWrapper wrapper;
    
    JythonModelCache(JythonWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    protected boolean isCacheable(Object object) {
        return true;
    }
    
    @Override
    protected TemplateModel create(Object obj) {
        boolean asHash = false;
        boolean asSequence = false;
        // If it's not a PyObject, first make a PyObject out of it.
        if(!(obj instanceof PyObject)) {
            obj = Py.java2py(obj);
        }
        if(asHash || obj instanceof PyDictionary || obj instanceof PyStringMap) {
            return JythonHashModel.FACTORY.create(obj, wrapper);
        }
        if(asSequence || obj instanceof PySequence) {
            return JythonSequenceModel.FACTORY.create(obj, wrapper);
        }
        if(obj instanceof PyInteger || obj instanceof PyLong || obj instanceof PyFloat) {
            return JythonNumberModel.FACTORY.create(obj, wrapper);
        }
        if(obj instanceof PyNone) {
            return TemplateModel.JAVA_NULL;
        }
        return JythonModel.FACTORY.create(obj, wrapper);
    }
}
