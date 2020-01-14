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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache storage that implements a two-level Most Recently Used cache. In the
 * first level, items are strongly referenced up to the specified maximum. When
 * the maximum is exceeded, the least recently used item is moved into the  
 * second level cache, where they are softly referenced, up to another 
 * specified maximum. When the second level maximum is also exceeded, the least 
 * recently used item is discarded altogether. This cache storage is a 
 * generalization of both {@link StrongCacheStorage} and 
 * {@link SoftCacheStorage} - the effect of both of them can be achieved by 
 * setting one maximum to zero and the other to the largest positive integer. 
 * On the other hand, if you wish to use this storage in a strong-only mode, or
 * in a soft-only mode, you might consider using {@link StrongCacheStorage} or
 * {@link SoftCacheStorage} instead, as they can be used by 
 * {@link TemplateCache} concurrently without any synchronization on a 5.0 or 
 * later JRE. 
 * This class is <em>NOT</em> thread-safe. If it is accessed from multiple
 * threads concurrently, proper synchronization must be provided by the callers.
 * Note that {@link TemplateCache}, the natural user of this class provides the
 * necessary synchronizations when it uses the class.
 * Also you might consider whether you need this sort of a mixed storage at all
 * in your solution, as in most cases SoftCacheStorage can also be sufficient. 
 * SoftCacheStorage will use Java soft references, and they already use access 
 * timestamps internally to bias the garbage collector against clearing 
 * recently used references, so you can get reasonably good (and 
 * memory-sensitive) most-recently-used caching through 
 * {@link SoftCacheStorage} as well.
 * @author Attila Szegedi
 * @version $Id: MruCacheStorage.java,v 1.7 2003/12/18 16:20:07 ddekany Exp $
 */
public class MruCacheStorage implements CacheStorage
{
    private final MruEntry<Object> strongHead = new MruEntry<Object>();
    private final MruEntry<Object> softHead = new MruEntry<Object>();
    {
        softHead.linkAfter(strongHead);
    }
    private final Map<Object, Object> map = new HashMap<Object, Object>();
    private final ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();
    private final int maxStrongSize;
    private final int maxSoftSize;
    private int strongSize = 0;
    private int softSize = 0;
    
    /**
     * Creates a new MRU cache storage with specified maximum cache sizes. Each
     * cache size can vary between 0 and {@link Integer#MAX_VALUE}.
     * @param maxStrongSize the maximum number of strongly referenced templates
     * @param maxSoftSize the maximum number of softly referenced templates
     */
    public MruCacheStorage(int maxStrongSize, int maxSoftSize) {
        if(maxStrongSize < 0) throw new IllegalArgumentException("maxStrongSize < 0");
        if(maxSoftSize < 0) throw new IllegalArgumentException("maxSoftSize < 0");
        this.maxStrongSize = maxStrongSize;
        this.maxSoftSize = maxSoftSize;
    }
    
    public Object get(Object key) {
        removeClearedReferences();
        MruEntry entry = (MruEntry)map.get(key);
        if(entry == null) {
            return null;
        }
        relinkEntryAfterStrongHead(entry, null);
        Object value = entry.getValue();
        if(value instanceof MruReference) {
            // This can only happen with maxStrongSize == 0
            return ((MruReference)value).get();
        }
        return value;
    }

    public void put(Object key, Object value) {
        removeClearedReferences();
        MruEntry<Object> entry = (MruEntry)map.get(key);
        if(entry == null) {
            entry = new MruEntry<Object>(key, value);
            map.put(key, entry);
            linkAfterStrongHead(entry);
        }
        else {
            relinkEntryAfterStrongHead(entry, value);
        }
        
    }

    public void remove(Object key) {
        removeClearedReferences();
        removeInternal(key);
    }
    
    private void removeInternal(Object key) {
        MruEntry entry = (MruEntry)map.remove(key);
        if(entry != null) {
            unlinkEntryAndInspectIfSoft(entry);
        }
    }

    public void clear() {
        strongHead.makeHead();
        softHead.linkAfter(strongHead);
        map.clear();
        strongSize = softSize = 0;
        // Quick refQueue processing
        while(refQueue.poll() != null) { /* Do nothing */ }
    }

    private void relinkEntryAfterStrongHead(MruEntry entry, Object newValue) {
        if(unlinkEntryAndInspectIfSoft(entry) && newValue == null) {
            // Turn soft reference into strong reference, unless is was cleared
            MruReference mref = (MruReference)entry.getValue();
            Object strongValue = mref.get();
            if (strongValue != null) {
                entry.setValue(strongValue);
                linkAfterStrongHead(entry);
            } else {
                map.remove(mref.getKey());
            }
        } else {
            if (newValue != null) {
                entry.setValue(newValue);
            }
            linkAfterStrongHead(entry);
        }
    }

    private void linkAfterStrongHead(MruEntry entry) {
        entry.linkAfter(strongHead);
        if(strongSize == maxStrongSize) {
            // softHead.previous is LRU strong entry
            MruEntry lruStrong = softHead.getPrevious();
            // Attila: This is equaivalent to maxStrongSize != 0
            // DD: But entry.linkAfter(strongHead) was just excuted above, so
            //     lruStrong != strongHead is true even if maxStrongSize == 0.
            if(lruStrong != strongHead) {
                lruStrong.unlink();
                if(maxSoftSize > 0) {
                    lruStrong.linkAfter(softHead);
                    lruStrong.setValue(new MruReference<Object>(lruStrong, refQueue));
                    if(softSize == maxSoftSize) {
                        // List is circular, so strongHead.previous is LRU soft entry
                        MruEntry lruSoft = strongHead.getPrevious();
                        lruSoft.unlink();
                        map.remove(lruSoft.getKey());
                    }
                    else {
                        ++softSize;
                    }
                }
                else {
                    map.remove(lruStrong.getKey());
                }
            }
        }
        else {
            ++strongSize;
        }
    }

    private boolean unlinkEntryAndInspectIfSoft(MruEntry entry) {
        entry.unlink();
        if(entry.getValue() instanceof MruReference) {
            --softSize;
            return true;
        }
        else {
            --strongSize;
            return false;
        }
    }
    
    private void removeClearedReferences() {
        for(;;) {
            MruReference<Object> ref = (MruReference<Object>)refQueue.poll();
            if(ref == null) {
                break;
            }
            removeInternal(ref.getKey());
        }
    }
    
    private static final class MruEntry<T>
    {
        private MruEntry<T> prev;
        private MruEntry<T> next;
        private final Object key;
        private T value;
        
        /**
         * Used solely to construct the head element
         */
        MruEntry()
        {
            makeHead();
            key = value = null;
        }
        
        MruEntry(Object key, T value) {
            this.key = key;
            this.value = value;
        }
        
        Object getKey() {
            return key;
        }
        
        T getValue() {
            return value;
        }
        
        void setValue(T value) {
            this.value = value;
        }

        MruEntry getPrevious() {
            return prev;
        }
        
        void linkAfter(MruEntry entry) {
            next = entry.next;
            entry.next = this;
            prev = entry;
            next.prev = this;
        }
        
        void unlink() {
            next.prev = prev;
            prev.next = next;
            prev = null;
            next = null;
        }
        
        void makeHead() {
            prev = next = this;
        }
    }
    
    private static class MruReference<T> extends SoftReference<T>
    {
        private final Object key;
        
        MruReference(MruEntry<T> entry, ReferenceQueue<Object> queue) {
            super(entry.getValue(), queue);
            this.key = entry.getKey();
        }
        
        Object getKey() {
            return key;
        }
    }
}