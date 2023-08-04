package freemarker.template;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import freemarker.ext.beans.ObjectWrapper;

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
public class SimpleHash implements TemplateHashModelEx {
    private Map map;
    private boolean putFailed;

    public SimpleHash() {
        map = new HashMap(); 
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
        this.map = map;
    }

    /**
     * Adds a key-value entry to the map.
     *
     * @param key the name by which the object is
     * identified in the template.
     * @param obj the object to store.
     */
    public void put(String key, Object obj) {
    	if (obj == null) obj = Constants.JAVA_NULL;
        map.put(key, obj);
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
                    return map.containsKey(key) || map.containsKey(charKey) ? Constants.JAVA_NULL : null;
                }
                else {
                    putKey = charKey;
                }
            }
            else {
                return map.containsKey(key) ? Constants.JAVA_NULL : null;
            }
        }
        else {
            putKey = key;
        }
        if (result instanceof TemplateModel) {
            return (TemplateModel) result;
        }
        Object tm = ObjectWrapper.instance().wrap(result);
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
        return new SimpleCollection(map.keySet());
    }

    public TemplateCollectionModel values() {
        return new SimpleCollection(map.values());
    }
}
