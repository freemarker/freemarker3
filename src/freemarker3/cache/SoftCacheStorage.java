package freemarker3.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Soft cache storage is a cache storage that uses {@link SoftReference} 
 * objects to hold the objects it was passed, therefore allows the garbage
 * collector to purge the cache when it determines that it wants to free up
 * memory.
 * This class is thread-safe to the extent that its underlying map is. The 
 * default implementation uses a concurrent map on Java 5 and above, so it is
 * thread-safe in that case.
 * @author Attila Szegedi
 * @version $Id: SoftCacheStorage.java,v 1.4 2003/09/22 20:47:03 ddekany Exp $
 *
 */
public class SoftCacheStorage implements ConcurrentCacheStorage
{
    private final ReferenceQueue queue = new ReferenceQueue();
    private final Map map;
    private final boolean concurrent;
    
    public SoftCacheStorage() {
        this(new ConcurrentHashMap());
    }
    
    public boolean isConcurrent() {
        return concurrent;
    }
    
    public SoftCacheStorage(Map backingMap) {
        map = backingMap;
        this.concurrent = map instanceof ConcurrentMap;
    }
    
    public Object get(Object key) {
        processQueue();
        Reference ref = (Reference)map.get(key);
        return ref == null ? null : ref.get();
    }

    public void put(Object key, Object value) {
        processQueue();
        map.put(key, new SoftValueReference(key, value, queue));
    }

    public void remove(Object key) {
        processQueue();
        map.remove(key);
    }

    public void clear() {
        map.clear();
        processQueue();
    }

    private void processQueue() {
        for(;;) {
            SoftValueReference ref = (SoftValueReference)queue.poll();
            if(ref == null) {
                return;
            }
            Object key = ref.getKey();
            if(concurrent) {
                ((ConcurrentMap)map).remove(key, ref);
            }
            else if(map.get(key) == ref) {
                map.remove(key);
            }
        }
    }

    private static final class SoftValueReference extends SoftReference {
        private final Object key;

        SoftValueReference(Object key, Object value, ReferenceQueue queue) {
            super(value, queue);
            this.key = key;
        }

        Object getKey() {
            return key;
        }
    }
}