package freemarker.ext.beans;

import java.util.List;
import java.util.Map;

import freemarker.core.ast.CollectionAndSequence;
import freemarker.ext.util.ModelFactory;
import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;

/**
 * Model used by {@link BeansWrapper} when <tt>simpleMapWrapper</tt>
 * mode is enabled. Provides a simple hash model interface to the
 * underlying map (does not copy like {@link freemarker.template.SimpleHash}),
 * and a method interface to non-string keys.
 * @author Chris Nokleberg
 * @version $Id: SimpleMapModel.java,v 1.9 2005/06/12 19:03:04 szegedia Exp $
 */
public class SimpleMapModel extends WrappingTemplateModel 
implements TemplateHashModelEx, TemplateMethodModelEx, AdapterTemplateModel, 
WrapperTemplateModel 
{
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object, ObjectWrapper wrapper)
            {
                return new SimpleMapModel((Map)object, (BeansWrapper)wrapper);
            }
        };

    private final Map map;
    
    public SimpleMapModel(Map map, BeansWrapper wrapper)
    {
        super(wrapper);
        this.map = map;
    }

    public Object get(String key) {
        Object val = map.get(key);
        if(val == null) {
            if(key.length() == 1) {
                // just check for Character key if this is a single-character string
                Character charKey = Character.valueOf(key.charAt(0));
                val = map.get(charKey);
                if (val == null) {
                    return (map.containsKey(key) || map.containsKey(charKey)) ? JAVA_NULL : null;
                }
            }
            else {
                return map.containsKey(key) ? JAVA_NULL : null;
            }
        }
        return wrap(val);
    }
    
    public Object exec(List args) {
        Object key = ((BeansWrapper)getObjectWrapper()).unwrap((TemplateModel)args.get(0));
        Object value = map.get(key);
        if (value == null && !map.containsKey(key)) {
            return null;
        }
        return wrap(value);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public TemplateCollectionModel keys() {
        return new CollectionAndSequence(new SimpleSequence(map.keySet(), getObjectWrapper()));
    }

    public TemplateCollectionModel values() {
        return new CollectionAndSequence(new SimpleSequence(map.values(), getObjectWrapper()));
    }
    
    public Object getAdaptedObject(Class hint) {
        return map;
    }
    
    public Object getWrappedObject() {
        return map;
    }
}
