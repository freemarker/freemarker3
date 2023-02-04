package freemarker.ext.beans;

import java.util.HashMap;
import java.util.Map;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.ClassUtil;

/**
 * Base class for hash models keyed by Java class names. 
 * @author Attila Szegedi
 * @version $Id: ClassBasedModelFactory.java,v 1.1 2005/11/03 08:49:19 szegedia Exp $
 */
abstract class ClassBasedModelFactory implements TemplateHashModel {
    private final BeansWrapper wrapper;
    private final Map<String, TemplateModel> cache = new HashMap<String, TemplateModel>();
    
    protected ClassBasedModelFactory(BeansWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public TemplateModel get(String key) throws TemplateModelException {
        synchronized(cache) {
            TemplateModel model = cache.get(key);
            if(model == null) {
                try {
                    Class clazz = ClassUtil.forName(key);
                    model = createModel(clazz);
                    // This is called so that we trigger the
                    // class-reloading detector. If there was a class reload,
                    // the wrapper will in turn call our clearCache method.
                    wrapper.introspectClass(clazz);
                } catch(Exception e) {
                    throw new TemplateModelException(e);
                }
                cache.put(key, model);
            }
            return model;
        }
    }
    
    void clearCache() {
        synchronized(cache) {
            cache.clear();
        }
    }

    void removeIntrospectionInfo(Class clazz) {
        synchronized(cache) {
            cache.remove(clazz.getName());
        }
    }

    public boolean isEmpty() {
        return false;
    }
    
    protected abstract TemplateModel createModel(Class clazz) 
    throws TemplateModelException;
    
    protected BeansWrapper getWrapper() {
        return wrapper;
    }
}
