package freemarker.core.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import freemarker.core.AbstractScope;
import freemarker.core.Scope;
import freemarker.template.SimpleCollection;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class NamedParameterListScope extends AbstractScope {
    private final List<String> paramNames;
    private final List<TemplateModel> paramValues;
    private final boolean readOnly;
    
    public NamedParameterListScope(Scope enclosingScope, 
            List<String> paramNames, List<TemplateModel> paramValues, boolean
            readOnly) {
        super(enclosingScope);
        this.paramNames = paramNames;
        this.paramValues = paramValues;
        this.readOnly = readOnly;
    }

    public boolean definesVariable(String name) {
        return paramNames.contains(name);
    }

    public Collection<String> getDirectVariableNames() {
        return paramNames;
    }

    public void put(String key, TemplateModel value) {
        if(readOnly) {
            throw new UnsupportedOperationException();
        }
        int i = paramNames.indexOf(key);
        if(i == -1) {
            throw new IllegalArgumentException("key " + key + " not found");
        }
        while(i >= paramValues.size()) {
            paramValues.add(null);
        }
        paramValues.set(i, value);
    }

    public TemplateModel remove(String key) {
        throw new UnsupportedOperationException();
    }

    public TemplateCollectionModel keys() {
        int size = Math.min(paramNames.size(), paramValues.size());
        List<SimpleScalar> nonNullValueKeys = new ArrayList<SimpleScalar>(size);
        for(int i = 0; i < size; ++i) {
            if(paramValues.get(i) != null) {
                nonNullValueKeys.add(new SimpleScalar(paramNames.get(i)));
            }
        }
        return new SimpleCollection(nonNullValueKeys);
    }
    
    public int size() {
        int nonNullCount = 0;
        int size = Math.min(paramNames.size(), paramValues.size());
        for(int i = 0; i < size; ++i) {
            if(paramValues.get(i) != null) {
                ++nonNullCount;
            }
        }
        return nonNullCount;
    }

    public TemplateCollectionModel values()  {
        int size = Math.min(paramNames.size(), paramValues.size());
        List<TemplateModel> nonNullValues = new ArrayList<TemplateModel>(size);
        for(int i = 0; i < size; ++i) {
            TemplateModel value = paramValues.get(i);
            if(value != null) {
                nonNullValues.add(value);
            }
        }
        return new SimpleCollection(nonNullValues);
    }

    public TemplateModel get(String key) {
        int i = paramNames.indexOf(key);
        return i != -1 && i < paramValues.size() ? paramValues.get(i) : null;
    }
}
