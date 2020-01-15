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
