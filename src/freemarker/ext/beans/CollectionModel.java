package freemarker.ext.beans;

import java.util.Collection;
import java.util.Iterator;

;

/**
 * <p>A special case of {@link Pojo} that can wrap Java collections
 * and that implements the {@link Iterable} in order to be usable 
 * in a <tt>&lt;foreach></tt> block.</p>
 * @author Attila Szegedi
 * @version $Id: CollectionModel.java,v 1.22 2003/06/03 13:21:32 szegedia Exp $
 */
public class CollectionModel extends StringModel implements Iterable
{
    /**
     * Creates a new model that wraps the specified collection object.
     * @param collection the collection object to wrap into a model.
     */
    public CollectionModel(Collection collection)
    {
        super(collection);
    }

    public Iterator<Object> iterator()
    {
        return new IteratorModel(((Collection)object).iterator());
    }
}
