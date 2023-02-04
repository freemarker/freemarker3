package freemarker.template;

/**
 * This interface is used to iterate over a set of template models, and is usually
 * returned from an instance of {@link TemplateCollectionModel}.
 * @author Attila Szegedi, szegedia at users dot sourceforge dot net
 * @version $Id: TemplateModelIterator.java,v 1.10 2003/01/12 23:40:21 revusky Exp $
 */
public interface TemplateModelIterator {

    /**
     * Returns the next model.
     * @throws TemplateModelException if the next model can not be retrieved
     * (i.e. because the iterator is exhausted).
     */
    TemplateModel next() throws TemplateModelException;

    /**
     * @return whether there are any more items to iterate over.
     */
    boolean hasNext() throws TemplateModelException;
}
