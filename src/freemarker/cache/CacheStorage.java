package freemarker.cache;

/**
 * Cache storage abstracts away the storage aspects of a cache - associating
 * an object with a key, retrieval and removal via the key. It is actually a
 * small subset of the {@link java.util.Map} interface. 
 * The implementations can be coded in a non-threadsafe manner as the natural
 * user of the cache storage, {@link TemplateCache} does the necessary
 * synchronization.
 * @author Attila Szegedi
 * @version $Id: CacheStorage.java,v 1.2 2003/08/08 10:10:58 szegedia Exp $
 */
public interface CacheStorage
{
    /**
     * Retrieve a cached value associated with a key
     * @param key the key for retrieving an associated value
     * @return the cached value associated with the key, or null if no value is
     * associated with the key.
     */
    public Object get(Object key);
    
    /**
     * Associates a key with a cached value
     * @param key the key to associate with a value
     * @param value the value associated with the key.
     */
    public void put(Object key, Object value);
    
    /**
     * Removes the value associated with a key, if it exists. If it doesn't
     * exist, this method does nothing.
     * @param key the key whose associated value is removed from the cache.
     */
    public void remove(Object key);
    
    /**
     * Removes all values from this cache
     */
    public void clear();
}
