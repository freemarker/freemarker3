package freemarker.core.helpers;

import java.util.Collection;
import java.util.Map;

import freemarker.core.AbstractScope;
import freemarker.core.Scope;
import freemarker.ext.beans.CollectionModel;
;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class NamedParameterMapScope extends AbstractScope {
    private final Map<String, Object> parameters;
    
    public NamedParameterMapScope(Scope enclosingScope, 
            Map<String, Object> parameters) {
        super(enclosingScope);
        this.parameters = parameters;
    }

    public boolean definesVariable(String name) {
        return parameters.containsKey(name);
    }

    public Collection<String> getDirectVariableNames() {
        return parameters.keySet();
    }

    public void put(String key, Object value) {
        parameters.put(key, value);
    }

    public Object remove(String key) {
        return parameters.remove(key);
    }

    public Iterable keys() {
        return new CollectionModel(parameters.keySet());
    }
    
    public int size() {
        return parameters.size();
    }

    public Iterable values()  {
        return new CollectionModel(parameters.values());
    }

    public Object get(String key) {
        return parameters.get(key);
    }
}
