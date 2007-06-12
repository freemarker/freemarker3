/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Strong cache storage is a cache storage that uses {@link SoftReference} 
 * objects to hold the objects it was passed, therefore allows the garbage
 * collector to purge the cache when it determines that it wants to free up
 * memory.
 * This class is <em>NOT</em> thread-safe. If it is accessed from multiple
 * threads concurrently, proper synchronization must be provided by the callers.
 * Note that {@link TemplateCache}, the natural user of this class provides the
 * necessary synchronizations when it uses the class.
 * @author Attila Szegedi
 * @version $Id: SoftCacheStorage.java,v 1.4 2003/09/22 20:47:03 ddekany Exp $
 *
 * @deprecated use {@link MruCacheStorage} instead.
 */
public class SoftCacheStorage implements CacheStorage
{
    private final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();
    private final Map<Object, ? super Reference> map;
    
    /**
     * Creates a new soft cache storage
     */
    public SoftCacheStorage()
    {
        this(new HashMap<Object, Reference>());
    }
    
    /**
     * Creates a new soft cache storage, using the specified map as its backing
     * map.
     * @param backingMap the map backing this storage
     */
    public SoftCacheStorage(Map<Object, ? super Reference> backingMap)
    {
        this.map = backingMap;
    }
    
    public Object get(Object key)
    {
        processQueue();
        Reference ref = (Reference)map.get(key);
        return ref == null ? null : ref.get();
    }

    public void put(Object key, Object value)
    {
        processQueue();
        map.put(key, new SoftValueReference(key, value, queue));
    }

    public void remove(Object key)
    {
        processQueue();
        map.remove(key);
    }

    public void clear()
    {
        map.clear();
        processQueue();
    }

    private void processQueue()
    {
        for(;;)
        {
            SoftValueReference<Object> ref = (SoftValueReference<Object>)queue.poll();
            if(ref == null)
            {
                return;
            }
            Object key = ref.getKey();
            // First check for reference identity, so that we don't remove
            // a reloaded template with the same key inadvertently.
            if(map.get(key) == ref)
            {
                map.remove(key);
            }
        }
    }

    private static final class SoftValueReference<T>
    extends
        SoftReference<T>
    {
        private final Object key;

        SoftValueReference(Object key, T value, ReferenceQueue<Object> queue)
        {
            super(value, queue);
            this.key = key;
        }

        Object getKey()
        {
            return key;
        }
    }
}
