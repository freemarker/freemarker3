/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package freemarker.cache;

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