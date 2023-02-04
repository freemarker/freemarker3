package freemarker.core.helpers;

import java.util.Collection;
import java.util.Map;

import freemarker.core.AbstractScope;
import freemarker.core.Scope;
import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class NamedParameterMapScope extends AbstractScope {
    private final Map<String, TemplateModel> parameters;
    
    public NamedParameterMapScope(Scope enclosingScope, 
            Map<String, TemplateModel> parameters) {
        super(enclosingScope);
        this.parameters = parameters;
    }

    public boolean definesVariable(String name) {
        return parameters.containsKey(name);
    }

    public Collection<String> getDirectVariableNames() {
        return parameters.keySet();
    }

    public void put(String key, TemplateModel value) {
        parameters.put(key, value);
    }

    public TemplateModel remove(String key) {
        return parameters.remove(key);
    }

    public TemplateCollectionModel keys() {
        return new SimpleCollection(parameters.keySet());
    }
    
    public int size() {
        return parameters.size();
    }

    public TemplateCollectionModel values()  {
        return new SimpleCollection(parameters.values());
    }

    public TemplateModel get(String key) {
        return parameters.get(key);
    }
}
