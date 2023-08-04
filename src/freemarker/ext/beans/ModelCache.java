package freemarker.ext.beans;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import freemarker.template.Constants;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelAdapter;

/**
 * Internally used by various wrapper implementations to implement model
 * caching.
 * @version $Id: ModelCache.java,v 1.9 2003/01/12 23:40:15 revusky Exp $
 * @author Attila Szegedi
 */
public class ModelCache
{
    private Map modelCache = null;
    private final Map<Class,ModelFactory> classToFactory = new ConcurrentHashMap<Class,ModelFactory>();
    private final Set<String> mappedClassNames = new HashSet<String>();

    //private final ObjectWrapper wrapper;

    
    public TemplateModel getInstance(Object object)
    {
        if(object == null) {
            return Constants.JAVA_NULL;
        }
        if(object instanceof TemplateModel) {
            return (TemplateModel)object;
        }
        if(object instanceof TemplateModelAdapter) {
            return ((TemplateModelAdapter)object).getTemplateModel();
        }
        return create(object);
    }
    
    public void clearCache()
    {
        if(modelCache != null)
        {
            synchronized(modelCache)
            {
                modelCache.clear();
            }
        }
    }

    TemplateModel create(Object object) {
        Class clazz = object.getClass();
        
        ModelFactory factory = classToFactory.get(clazz);
        if(factory == null) {
            synchronized(mappedClassNames) {
                factory = classToFactory.get(clazz);
                if(factory == null) {
                    String className = clazz.getName();
                    // clear mappings when class reloading is detected
                    if(!mappedClassNames.add(className)) {
                        classToFactory.clear();
                        mappedClassNames.clear();
                        mappedClassNames.add(className);
                    }
                    factory = ObjectWrapper.instance().getModelFactory(clazz);
                    classToFactory.put(clazz, factory);
                }
            }
        }
        return factory.create(object, ObjectWrapper.instance());
    }
}