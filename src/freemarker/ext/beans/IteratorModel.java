package freemarker.ext.beans;

import java.util.Iterator;
import java.util.NoSuchElementException;

import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModelException;
import static freemarker.ext.beans.ObjectWrapper.wrap;

/**
 * <p>A class that adds Iterator functionality to the
 * {@link Iterator} interface implementers. 
 * </p>
 * <p>It differs from the {@link freemarker.template.CollectionModel} in that 
 * it inherits from {@link Pojo}, and therefore you can call methods on 
 * it directly, even to the effect of calling <tt>iterator.remove()</tt> in 
 * the template.</p> <p>Using the model as a collection model is NOT 
 * thread-safe, as iterators are inherently not thread-safe.
 * Further, you can iterate over it only once. Attempts to call the
 * {@link #iterator()} method after it was already driven to the end once will 
 * throw an exception.</p>
 * @author Attila Szegedi
 * @version $Id: IteratorModel.java,v 1.26 2003/06/03 13:21:32 szegedia Exp $
 */

public class IteratorModel extends Pojo implements Iterator<Object>, TemplateCollectionModel
{
    private boolean accessed = false;
    
    /**
     * Creates a new model that wraps the specified iterator object.
     * @param iterator the iterator object to wrap into a model.
     */
    public IteratorModel(Iterator iterator)
    {
        super(iterator);
    }

    /**
     * This allows the iterator to be used in a <tt>&lt;foreach></tt> block.
     * @return "this"
     */
    public Iterator<Object> iterator() {
        synchronized(this) {
            if(accessed) {
                throw new TemplateModelException(
                    "This collection is stateful and can not be iterated over the" +
                    " second time.");
            }
            accessed = true;
        }
        return this;
    }
    
    /**
     * Calls underlying {@link Iterator#hasNext()}.
     */
    public boolean hasNext() {
        return ((Iterator)object).hasNext();
    }


    /**
     * Calls underlying {@link Iterator#next()} and wraps the result.
     */
    public Object next() {
        try {
            return wrap(((Iterator)object).next());
        }
        catch(NoSuchElementException e) {
            throw new TemplateModelException(
                "No more elements in the iterator.", e);
        }
    }

    /**
     * Returns {@link Iterator#hasNext()}. Therefore, an
     * iterator that has no more element evaluates to false, and an 
     * iterator that has further elements evaluates to true.
     */
    public boolean getAsBoolean() {
        return hasNext();
    }
}
