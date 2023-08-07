package freemarker.ext.beans;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import freemarker.core.ast.CollectionAndSequence;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.Constants;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;

/**
 * Model used by {@link ObjectWrapper} when <tt>simpleMapWrapper</tt>
 * mode is enabled. Provides a simple hash model interface to the
 * underlying map (does not copy like {@link freemarker.template.SimpleMapModel}),
 * and a method interface to non-string keys.
 * @author Chris Nokleberg
 * @version $Id: SimpleMapModel.java,v 1.9 2005/06/12 19:03:04 szegedia Exp $
 */
public class SimpleMapModel implements TemplateHashModelEx, TemplateMethodModelEx, AdapterTemplateModel 
{
    private final Map map;

    public SimpleMapModel() {
        map = new HashMap();
    }
    
    public SimpleMapModel(Map map)
    {
        this.map = map;
    }

    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    public Object get(String key) {
        Object val = map.get(key);
        if(val == null) {
            if(key.length() == 1) {
                // just check for Character key if this is a single-character string
                Character charKey = Character.valueOf(key.charAt(0));
                val = map.get(charKey);
                if (val == null) {
                    return (map.containsKey(key) || map.containsKey(charKey)) ? Constants.JAVA_NULL : null;
                }
            }
            else {
                return map.containsKey(key) ? Constants.JAVA_NULL : null;
            }
        }
        return ObjectWrapper.instance().wrap(val);
    }
    
    public Object exec(List args) {
        Object key = ((TemplateModel)args.get(0)).unwrap();
        Object value = map.get(key);
        if (value == null && !map.containsKey(key)) {
            return null;
        }
        return ObjectWrapper.instance().wrap(value);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public TemplateCollectionModel keys() {
        return new CollectionAndSequence(new ListModel(new ArrayList(map.keySet())));
    }

    public TemplateCollectionModel values() {
        return new CollectionAndSequence(new ListModel(new ArrayList(map.values())));
    }
    
    public Object getAdaptedObject(Class hint) {
        return map;
    }
    
    public Object getWrappedObject() {
        return map;
    }
}
