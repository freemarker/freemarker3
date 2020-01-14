/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package freemarker.ext.beans;

import java.util.List;
import java.util.Map;
import java.util.Set;

import freemarker.ext.util.ModelFactory;
import freemarker.template.ObjectWrapper;
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
                return new MapModel((Map)object, (BeansWrapper)wrapper);
            }
        };

    /**
     * Creates a new model that wraps the specified map object.
     * @param map the map object to wrap into a model.
     * @param wrapper the {@link BeansWrapper} associated with this model.
     * Every model has to have an associated {@link BeansWrapper} instance. The
     * model gains many attributes from its wrapper, including the caching 
     * behavior, method exposure level, method-over-item shadowing policy etc.
     */
    public MapModel(Map map, BeansWrapper wrapper)
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
        	return map.containsKey(key) ? JAVA_NULL : null;
        }
        return wrap(value);
    }

    /**
     * Overridden to invoke the generic get method by casting to Map instead of 
     * through reflection - should yield better performance.
     */
    protected TemplateModel invokeGenericGet(Map keyMap, String key)
    throws TemplateModelException
    {
        Map map = (Map) object;
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
