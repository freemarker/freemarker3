package freemarker3.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Strong cache storage is a cache storage that simply wraps a {@link Map}.
 * It holds a strong reference to all objects it was passed, therefore prevents
 * the cache from being purged during garbage collection.
 * This class is thread-safe to the extent that its underlying map is. The 
 * default implementation uses a concurrent map on Java 5 and above, so it is
 * thread-safe in that case.
 * @author Attila Szegedi
 * @version $Id: StrongCacheStorage.java,v 1.3 2003/09/22 20:47:03 ddekany Exp $
 *
 */
public class StrongCacheStorage implements ConcurrentCacheStorage
{
    private final Map map = new ConcurrentHashMap();

    public boolean isConcurrent() {
        return map instanceof ConcurrentMap;
    }
    
    public Object get(Object key) {
        return map.get(key);
    }

    public void put(Object key, Object value) {
        map.put(key, value);
    }

    public void remove(Object key) {
        map.remove(key);
    }
    
    public void clear() {
        map.clear();
    }
}
