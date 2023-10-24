package freemarker.ext.beans;

import java.util.AbstractCollection;
import java.util.Iterator;

import freemarker.template.TemplateModelAdapter;
import freemarker.template.EvaluationException;
import freemarker.template.utility.UndeclaredThrowableException;

/**
 * @author Attila Szegedi
 * @version $Id: CollectionAdapter.java,v 1.2 2005/06/12 19:03:04 szegedia Exp $
 */
class CollectionAdapter extends AbstractCollection implements TemplateModelAdapter {
    private final ObjectWrapper wrapper;
    private final Iterable model;
    
    CollectionAdapter(Iterable model, ObjectWrapper wrapper) {
        this.model = model;
        this.wrapper = wrapper;
    }
    
    public Iterable getTemplateModel() {
        return model;
    }
    
    public int size() {
        throw new UnsupportedOperationException();
    }

    public Iterator iterator() {
        try {
            return new Iterator() {
                final Iterator<Object> i = model.iterator();
    
                public boolean hasNext() {
                    try {
                        return i.hasNext();
                    }
                    catch(EvaluationException e) {
                        throw new UndeclaredThrowableException(e);
                    }
                }
                
                public Object next() {
                    try {
                        return wrapper.unwrap(i.next());
                    }
                    catch(EvaluationException e) {
                        throw new UndeclaredThrowableException(e);
                    }
                }
                
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        catch(EvaluationException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
