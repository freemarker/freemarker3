package freemarker.ext.beans;

import java.util.List;
import java.util.Map;
import java.util.Set;

import freemarker.template.Constants;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * <p>A special case of {@link BeanModel} that adds implementation
 * for {@link TemplateMethodModelEx} on map objects that is a shortcut for the
 * <tt>Map.get()</tt> method. Note that if the passed argument itself is a
 * reflection-wrapper model, then the map lookup will be performed using the
 * wrapped object as the key. Note that you can call <tt>get()</tt> using the
 * <tt>map.key</tt> syntax inherited from {@link BeanModel} as well, 
 * however in that case the key is always a string.</p>
 * <p>The class itself does not implement the {@link freemarker.template.TemplateCollectionModel}.
 * You can, however use <tt>map.entrySet()</tt>, <tt>map.keySet()</tt>, or
 * <tt>map.values()</tt> to obtain {@link freemarker.template.TemplateCollectionModel} instances for 
 * various aspects of the map.</p>
 * @author Attila Szegedi
 * @version $Id: MapModel.java,v 1.28 2006/02/26 18:26:57 revusky Exp $
 */
public class MapModel
extends
    StringModel
implements
    TemplateMethodModelEx
{
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object, ObjectWrapper wrapper)
            {
                return new MapModel((Map)object, wrapper);
            }
        };

    /**
     * Creates a new model that wraps the specified map object.
     * @param map the map object to wrap into a model.
     * @param wrapper the {@link ObjectWrapper} associated with this model.
     * Every model has to have an associated {@link ObjectWrapper} instance. The
     * model gains many attributes from its wrapper, including the caching 
     * behavior, method exposure level, method-over-item shadowing policy etc.
     */
    public MapModel(Map map, ObjectWrapper wrapper)
    {
        super(map, wrapper);
    }

    /**
     * The first argument is used as a key to call the map's <tt>get</tt> method.
     */
    public Object exec(List arguments)
    throws
        TemplateModelException
    {
        Object key = unwrap((TemplateModel)arguments.get(0));
        Map map = (Map) object;
        Object value = map.get(key);
        if (value == null) {
        	return map.containsKey(key) ? Constants.JAVA_NULL : null;
        }
        return wrap(value);
    }

    /**
     * Overridden to invoke the generic get method by casting to Map instead of 
     * through reflection - should yield better performance.
     */
    protected Object invokeGenericGet(Map keyMap, String key) {
        Map map = (Map) object;
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
        return wrap(val);
    }

    public boolean isEmpty()
    {
        return ((Map)object).isEmpty() && super.isEmpty();
    }

    public int size()
    {
        return keySet().size();
    }

    protected Set keySet()
    {
        Set set = super.keySet();
        set.addAll(((Map)object).keySet());
        return set;
    }
}
