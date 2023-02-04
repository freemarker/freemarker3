package freemarker.template;

/**
 * List values in a template data model whose elements are accessed by the 
 * index operator should implement this interface. In addition to
 * accessing elements by index and querying size using the <code>?size</code>
 * built-in, objects that implement this interface can be iterated in 
 * <code>&lt;#foreach ...></code> and <code>&lt;#list ...></code> directives. The 
 * iteration is implemented by calling the {@link #get(int)} method 
 * repeatedly starting from zero and going to <tt>{@link #size()} - 1</tt>.
 * @author Attila Szegedi, szegedia at users dot sourceforge dot net
 * @version $Id: TemplateSequenceModel.java,v 1.10 2004/11/27 14:49:57 ddekany Exp $
 */
public interface TemplateSequenceModel extends TemplateModel {

    /**
     * Retrieves the i-th template model in this sequence.
     * 
     * @return the item at the specified index, or <code>null</code> if
     * the index is out of bounds. Note that a <code>null</code> value is
     * interpreted by FreeMarker as "variable does not exist", and accessing
     * a missing variables is usually considered as an error in the FreeMarker
     * Template Language, so the usage of a bad index will not remain hidden.
     */
    TemplateModel get(int index) throws TemplateModelException;

    /**
     * @return the number of items in the list.
     */
    int size() throws TemplateModelException;
}
