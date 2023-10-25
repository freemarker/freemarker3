package freemarker.template;

/**
 * Hashes in a data model must implement this interface. Hashes
 * are FreeMarker data objects that contain other objects through key-value 
 * mappings.
 *
 * @version $Id: TemplateHashModel.java,v 1.13 2005/06/08 02:13:34 revusky Exp $
 */
public interface TemplateHashModel extends WrappedVariable {
    
    /**
     * Gets a <tt>WrappedVariable</tt> from the hash.
     *
     * @param key the name by which the <tt>WrappedVariable</tt>
     * is identified in the template.
     * @return the <tt>WrappedVariable</tt> referred to by the key,
     * or null if not found.
     */
    Object get(String key);

    boolean isEmpty();
}
