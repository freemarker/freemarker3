package freemarker.core.variables;

/**
 * Hashes in a data model must implement this interface. Hashes
 * are FreeMarker data objects that contain other objects through key-value 
 * mappings.
 */
public interface Hash {
    
    /**
     * Gets a variable from the hash.
     *
     * @param key the name by which the <tt>WrappedVariable</tt>
     * is identified in the template.
     * @return the value referred to by the key,
     * or null if not found.
     */
    Object get(String key);

    default boolean isEmpty() {
        return false;
    }

   /**
     * @return the number of key/value mappings in the hash.
     */
    default int size() {
        throw new EvaluationException("Unsupported method size()");
    }

    /**
     * @return a collection containing the keys in the hash. 
     */
    default Iterable<?> keys() {
        throw new EvaluationException("Unsupported method keys()");
    }

    /**
     * @return a collection containing the values in the hash.
     */
    default Iterable<?> values() {
        throw new EvaluationException("Unsupported method values()");
    }
}
