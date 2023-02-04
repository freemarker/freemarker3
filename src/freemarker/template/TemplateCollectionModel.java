package freemarker.template;

/**
 * This interface can be implemented by a class to make a variable "foreach-able", 
 * i.e. the model can be used as the list in a &lt;foreach...&gt;
 * or a &lt;list...&gt; directive. Use this model when 
 * your collection does not support index-based access and possibly,
 * the size cannot be known in advance. If you need index-based
 * access, use a {@link TemplateSequenceModel} instead.
 * @see SimpleSequence
 * @see SimpleCollection
 *
 * @author Attila Szegedi, szegedia at users dot sourceforge dot net
 * @version $Id: TemplateCollectionModel.java,v 1.10 2003/01/12 23:40:21 revusky Exp $
 */
public interface TemplateCollectionModel extends TemplateModel {

    /**
     * Retrieves a template model iterator that is used to iterate over
     * the elements in this collection.
     */
    public TemplateModelIterator iterator() throws TemplateModelException;
}
