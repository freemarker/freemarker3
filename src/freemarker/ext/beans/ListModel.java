package freemarker.ext.beans;

import java.util.Collection;
import java.util.List;

import freemarker.ext.util.ModelFactory;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

/**
 * <p>A special case of {@link BeanModel} that can wrap Java lists
 * and that implements the {@link TemplateCollectionModel} and 
 * {@link TemplateSequenceModel} in order to be usable in a <tt>&lt;foreach></tt> block.</p>
 * @author Attila Szegedi
 * @version $Id: CollectionModel.java,v 1.22 2003/06/03 13:21:32 szegedia Exp $
 */
public class ListModel
extends
    CollectionModel
implements
    TemplateSequenceModel
{
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object, ObjectWrapper wrapper)
            {
                return new ListModel((List)object, (BeansWrapper)wrapper);
            }
        };


    /**
     * Creates a new model that wraps the specified collection object.
     * @param list the list object to wrap into a model.
     * @param wrapper the {@link BeansWrapper} associated with this model.
     * Every model has to have an associated {@link BeansWrapper} instance. The
     * model gains many attributes from its wrapper, including the caching 
     * behavior, method exposure level, method-over-item shadowing policy etc.
     */
    public ListModel(List list, BeansWrapper wrapper)
    {
        super(list, wrapper);
    }

    /**
     * Retrieves the i-th object from the collection, wrapped as a TemplateModel.
     * @return null if the index is out of bounds
     * *@throws TemplateModelException if the underlying collection is not a List.
     */
    public TemplateModel get(int index)
    throws
        TemplateModelException
    {
        try
        {
            return wrap(((List)object).get(index));
        }
        catch(IndexOutOfBoundsException e)
        {
            return null; // This is better because it allows the use of existence built-ins, i.e. x[10]?? etcetera (JR)
        }
    }
    
    public int size()
    {
        return ((Collection)object).size();
    }
}
