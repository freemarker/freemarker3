package freemarker.core;

import java.util.*;

import freemarker.ext.beans.CollectionModel;
import freemarker.template.*;

/**
 * A basic scope that stores variables locally in a hash map. 
 * @author Jonathan Revusky
 * @version $Id: $
 */
public class BaseScope extends AbstractScope {

    private Map<String,Object> variables = new HashMap<>();

    BaseScope(Scope enclosingScope) {
        super(enclosingScope);
    }

    public void put(String key, Object value) {
        variables.put(key, value);
    }

    public Object remove(String key) {
        return variables.remove(key);
    }

    public Object get(String key) { 
        return variables.get(key);
    }

    public boolean definesVariable(String key) {
        return variables.containsKey(key);
    }

    public boolean isEmpty() {
        return variables.isEmpty();
    }

    public TemplateCollectionModel keys() {
        return new CollectionModel(variables.keySet());
    }

    public TemplateCollectionModel values() {
        return new CollectionModel(variables.values());
    }


    public int size() {
        return variables.size();
    }

    public void clear() {
        variables.clear();
    }

    public Collection<String> getDirectVariableNames() {
        return Collections.unmodifiableCollection(variables.keySet());
    }
}
