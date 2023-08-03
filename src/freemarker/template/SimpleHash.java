package freemarker.template;

import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import freemarker.ext.beans.BeansWrapper;

/**
 * <p>A simple implementation of the <tt>TemplateHashModelEx</tt>
 * interface, using an underlying {@link Map} or {@link SortedMap}.</p>
 *
 * <p>This class is thread-safe if you don't call the <tt>put</tt> or <tt>remove</tt> methods
 * after you have made the object available for multiple threads.
 *
 * <p><b>Note:</b><br />
 * As of 2.0, this class is unsynchronized by default.
 * To obtain a synchronized wrapper, call the {@link #synchronizedWrapper} method.</p>
 *
 * @version $Id: SimpleHash.java,v 1.74 2006/02/26 18:27:14 revusky Exp $
 * @see SimpleSequence
 * @see SimpleScalar
 */
public class SimpleHash extends WrappingTemplateModel 
implements TemplateHashModelEx, Serializable {
    private static final long serialVersionUID = -5942587725941500249L;

    private Map map;
    private boolean putFailed;
    private Map unwrappedMap;

    /**
     * Constructs an empty hash that uses the default wrapper set in
     * {@link WrappingTemplateModel#setDefaultObjectWrapper(ObjectWrapper)}.
     */
    public SimpleHash() {
        this((ObjectWrapper)null);
    }

    /**
     * Creates a new simple hash with the copy of the underlying map and the
     * default wrapper set in 
     * {@link WrappingTemplateModel#setDefaultObjectWrapper(ObjectWrapper)}.
     * @param map The Map to use for the key/value pairs. It makes a copy for 
     * internal use. If the map implements the {@link SortedMap} interface, the
     * internal copy will be a {@link TreeMap}, otherwise it will be a 
     * {@link HashMap}.
     */
    public SimpleHash(Map map) {
        this(map, null);
    }

    /**
     * Creates an empty simple hash using the specified object wrapper.
     * @param wrapper The object wrapper to use to wrap objects into
     * {@link TemplateModel} instances. If null, the default wrapper set in 
     * {@link WrappingTemplateModel#setDefaultObjectWrapper(ObjectWrapper)} is
     * used.
     */
    public SimpleHash(ObjectWrapper wrapper) {
        super(wrapper);
        map = new HashMap();
    }

    /**
     * Creates a new simple hash with the copy of the underlying map and 
     * either the default wrapper set in 
     * {@link WrappingTemplateModel#setDefaultObjectWrapper(ObjectWrapper)}, or
     * the {@link freemarker.ext.beans.BeansWrapper JavaBeans wrapper}.
     * @param map The Map to use for the key/value pairs. It makes a copy for 
     * internal use. If the map implements the {@link SortedMap} interface, the
     * internal copy will be a {@link TreeMap}, otherwise it will be a 
     * @param wrapper The object wrapper to use to wrap objects into
     * {@link TemplateModel} instances. If null, the default wrapper set in 
     * {@link WrappingTemplateModel#setDefaultObjectWrapper(ObjectWrapper)} is
     * used.
     */
    public SimpleHash(Map map, ObjectWrapper wrapper) {
        super(wrapper);
        try {
            this.map = copyMap(map);
        } catch (ConcurrentModificationException cme) {
            //This will occur extremely rarely.
            //If it does, we just wait 5 ms and try again. If 
            // the ConcurrentModificationException
            // is thrown again, we just let it bubble up this time.
            // TODO: Maybe we should log here.
            try {
                Thread.sleep(5);
            } catch (InterruptedException ie) {
            }
            synchronized (map) {
                this.map = copyMap(map);
            }
        }
    }

    protected Map copyMap(Map map) {
        if (map instanceof HashMap) {
            return (Map) ((HashMap) map).clone();
        }
        if (map instanceof SortedMap) {
            if (map instanceof TreeMap) {
                return (Map) ((TreeMap) map).clone();
            }
            else {
                return new TreeMap((SortedMap) map);
            }
        } 
        return new LinkedHashMap(map);
    }

    /**
     * Adds a key-value entry to the map.
     *
     * @param key the name by which the object is
     * identified in the template.
     * @param obj the object to store.
     */
    public void put(String key, Object obj) {
    	if (obj == null) obj = JAVA_NULL;
        map.put(key, obj);
        unwrappedMap = null;
    }

    /**
     * Puts a boolean in the map
     *
     * @param key the name by which the resulting <tt>TemplateModel</tt>
     * is identified in the template.
     * @param b the boolean to store.
     */
    public void put(String key, boolean b) {
        put(key, b ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE);
    }

    public Object get(String key) {
        Object result = map.get(key);
        // The key to use for putting -- it is the key that already exists in
        // the map (either key or charKey below). This way, we'll never put a 
        // new key in the map, avoiding spurious ConcurrentModificationException
        // from another thread iterating over the map, see bug #1939742 in 
        // SourceForge tracker.
        final Object putKey;
        if (result == null) {
            if(key.length() == 1) {
                // just check for Character key if this is a single-character string
                Character charKey = Character.valueOf(key.charAt(0));
                result = map.get(charKey);
                if (result == null) {
                    return map.containsKey(key) || map.containsKey(charKey) ? JAVA_NULL : null;
                }
                else {
                    putKey = charKey;
                }
            }
            else {
                return map.containsKey(key) ? JAVA_NULL : null;
            }
        }
        else {
            putKey = key;
        }
        if (result instanceof TemplateModel) {
            return (TemplateModel) result;
        }
        Object tm = wrap(result);
        if (!putFailed) try {
            if (tm != null) map.put(putKey, tm);
        } catch (Exception e) {
            // If it's immutable or something, we just keep going.
            putFailed = true;
        }
        return tm;
    }


    /**
     * Removes the given key from the underlying map.
     *
     * @param key the key to be removed
     */
    public void remove(String key) {
        map.remove(key);
    }

    /**
     * Adds all the key/value entries in the map
     * @param m the map with the entries to add, the keys are assumed to be strings.
     */

    public void putAll(Map m) {
        for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            this.put((String) entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Note that this method creates and returns a deep-copy of the underlying hash used
     * internally. This could be a gotcha for some people
     * at some point who want to alter something in the data model,
     * but we should maintain our immutability semantics (at least using default SimpleXXX wrappers) 
     * for the data model. It will recursively unwrap the stuff in the underlying container. 
     */
    public Map toMap() {
        if (unwrappedMap == null) {
            Class mapClass = this.map.getClass();
            Map m = null;
            try {
                m = (Map) mapClass.newInstance();
            } catch (Exception e) {
                throw new TemplateModelException("Error instantiating map of type " + mapClass.getName() + "\n" + e.getMessage());
            }
            // Create a copy to maintain immutability semantics and
            // Do nested unwrapping of elements if necessary.
            BeansWrapper bw = BeansWrapper.getDefaultInstance();
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof TemplateModel) {
                    value = bw.unwrap((TemplateModel) value);
                }
                m.put(key, value);
            }
            unwrappedMap=m;
        }
        return unwrappedMap;
    }

    /**
     * Convenience method for returning the <tt>String</tt> value of the
     * underlying map.
     */
    public String toString() {
        return map.toString();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map == null || map.isEmpty();
    }

    public TemplateCollectionModel keys() {
        return new SimpleCollection(map.keySet(), getObjectWrapper());
    }

    public TemplateCollectionModel values() {
        return new SimpleCollection(map.values(), getObjectWrapper());
    }

    public SimpleHash synchronizedWrapper() {
        return new SynchronizedHash();
    }
    
    
    private class SynchronizedHash extends SimpleHash {
        private static final long serialVersionUID = -5266601722334529216L;

        public boolean isEmpty() {
            synchronized (SimpleHash.this) {
                return SimpleHash.this.isEmpty();
            }
        }
        
        public void put(String key, Object obj) {
            synchronized (SimpleHash.this) {
                SimpleHash.this.put(key, obj);
            }
        }

        public Object get(String key) {
            synchronized (SimpleHash.this) {
                return SimpleHash.this.get(key);
            }
        }

        public void remove(String key) {
            synchronized (SimpleHash.this) {
                SimpleHash.this.remove(key);
            }
        }

        public int size() {
            synchronized (SimpleHash.this) {
                return SimpleHash.this.size();
            }
        }

        public TemplateCollectionModel keys() {
            synchronized (SimpleHash.this) {
                return SimpleHash.this.keys();
            }
        }

        public TemplateCollectionModel values() {
            synchronized (SimpleHash.this) {
                return SimpleHash.this.values();
            }
        }
        
        public Map toMap() {
            synchronized (SimpleHash.this) {
                return SimpleHash.this.toMap();
            }
        }
    
    }
}
