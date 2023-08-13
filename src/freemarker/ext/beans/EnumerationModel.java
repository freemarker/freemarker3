package freemarker.ext.beans;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

;
import freemarker.template.TemplateModelException;
import static freemarker.ext.beans.ObjectWrapper.wrap;

/**
 * <p>A class that adds Iterator functionality to the
 * {@link Enumeration} interface implementers. 
 * </p> <p>Using the model as a collection model is NOT thread-safe, as 
 * enumerations are inherently not thread-safe.
 * Further, you can iterate over it only once. Attempts to call the
 * {@link #iterator()} method after it was already driven to the end once will 
 * throw an exception.</p>
 * @author Attila Szegedi
 * @version $Id: EnumerationModel.java,v 1.24 2003/06/03 13:21:32 szegedia Exp $
 */

public class EnumerationModel
extends
    Pojo
implements
    Iterator<Object>,
    Iterable
{
    private boolean accessed = false;
    
    /**
     * Creates a new model that wraps the specified enumeration object.
     * @param enumeration the enumeration object to wrap into a model.
     */
    public EnumerationModel(Enumeration<?> enumeration)
    {
        super(enumeration);
    }

    /**
     * This allows the enumeration to be used in a <tt>&lt;foreach></tt> block.
     * @return "this"
     */
    public Iterator<Object> iterator() throws TemplateModelException
    {
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
     * Calls underlying {@link Enumeration#nextElement()}.
     */
    public boolean hasNext() {
        return ((Enumeration)object).hasMoreElements();
    }


    /**
     * Calls underlying {@link Enumeration#nextElement()} and wraps the result.
     */
    public Object next() {
        try {
            return wrap(((Enumeration)object).nextElement());
        }
        catch(NoSuchElementException e) {
            throw new TemplateModelException(
                "No more elements in the enumeration.");
        }
    }

    /**
     * Returns {@link Enumeration#hasMoreElements()}. Therefore, an
     * enumeration that has no more element evaluates to false, and an 
     * enumeration that has further elements evaluates to true.
     */
    public boolean getAsBoolean() {
        return hasNext();
    }
}
