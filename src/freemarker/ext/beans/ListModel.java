package freemarker.ext.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

import static freemarker.ext.beans.ObjectWrapper.wrap;

/**
 * <p>A special case of {@link Pojo} that can wrap Java lists
 * and that implements the {@link Iterable} and 
 * {@link TemplateSequenceModel} in order to be usable in a <tt>&lt;foreach></tt> block.</p>
 * @author Attila Szegedi
 * @version $Id: CollectionModel.java,v 1.22 2003/06/03 13:21:32 szegedia Exp $
 */
public class ListModel extends CollectionModel implements TemplateSequenceModel {
    /**
     * Creates a new model that wraps the specified collection object.
     * @param list the list object to wrap into a model.
     */
    public ListModel(List list)
    {
        super(list);
    }

    public ListModel() {
        this(new ArrayList());
    }

    public void add(Object obj) {
        ((List)this.getWrappedObject()).add(obj);
    }

    /**
     * Retrieves the i-th object from the collection, wrapped as a TemplateModel.
     * @return null if the index is out of bounds
     * *@throws TemplateModelException if the underlying collection is not a List.
     */
    public Object get(int index) {
        try {
            return wrap(((List)object).get(index));
        }
        catch(IndexOutOfBoundsException e) {
            return null; // This is better because it allows the use of existence built-ins, i.e. x[10]?? etcetera (JR)
        }
    }
    
    public int size()
    {
        return ((Collection)object).size();
    }
}
