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

package freemarker.ext.script;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.script.ScriptContext;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Class that wraps a {@link ScriptContext} into a {@link TemplateHashModelEx}
 * @author Attila Szegedi
 * @version $Id: $
 */
class ScriptContextHashModel implements TemplateHashModelEx
{
    private final ScriptContext context;
    private final ObjectWrapper wrapper;
    
    ScriptContextHashModel(ScriptContext context, ObjectWrapper wrapper)
    {
        this.context = context;
        this.wrapper = wrapper;
    }

    public TemplateModel get(String key) throws TemplateModelException
    {
        Object retval;
        synchronized(context)
        {
            retval = context.getAttribute(key);
        }
        return wrapper.wrap(retval);
    }

    public boolean isEmpty() throws TemplateModelException
    {
        synchronized(context)
        {
            for(int scope : context.getScopes())
            {
                if(!context.getBindings(scope).isEmpty())
                {
                    return false;
                }
            }
        }
        return true;
    }

    public TemplateCollectionModel keys() throws TemplateModelException
    {
        Set<String> keys;
        synchronized(context)
        {
            keys = getAllKeys();
        }
        return (TemplateCollectionModel)wrapper.wrap(keys);
    }

    private Set<String> getAllKeys()
    {
        Set<String> keys = new HashSet<String>();
        for(int scope : context.getScopes())
        {
            keys.addAll(context.getBindings(scope).keySet());
        }
        return keys;
    }

    public int size()
    {
        Set<String> keys;
        synchronized(context)
        {
            keys = getAllKeys();
        }
        return keys.size();
    }

    public TemplateCollectionModel values() throws TemplateModelException
    {
        List<Object> values;
        synchronized(context)
        {
            Set<String> keys = getAllKeys();
            values = new ArrayList<Object>(keys.size());
            for (String key : keys)
            {
                int scope = context.getAttributesScope(key);
                if(scope != -1)
                {
                    Object value = context.getAttribute(key);
                    if(value != null)
                    {
                        values.add(value);
                    }
                }
            }
        }
        return (TemplateCollectionModel)wrapper.wrap(values);
    }
}