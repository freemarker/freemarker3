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

package freemarker.ext.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.util.ModelFactory;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

/**
 * @author Attila Szegedi
 * @version $Id: RhinoScriptableModel.java,v 1.4 2005/06/22 10:52:52 ddekany Exp $
 */
public class RhinoScriptableModel implements TemplateHashModelEx, 
TemplateSequenceModel, AdapterTemplateModel, TemplateScalarModel, 
TemplateBooleanModel, TemplateNumberModel
{
    static final ModelFactory FACTORY = new ModelFactory() {
        public TemplateModel create(Object object, ObjectWrapper wrapper) {
            return new RhinoScriptableModel((Scriptable)object, 
                    (BeansWrapper)wrapper);
        }
    };
    
    private final Scriptable scriptable;
    private final BeansWrapper wrapper;
    
    public RhinoScriptableModel(Scriptable scriptable, BeansWrapper wrapper) {
        this.scriptable = scriptable;
        this.wrapper = wrapper;
    }
    
    public TemplateModel get(String key) throws TemplateModelException {
        Object retval = ScriptableObject.getProperty(scriptable, key);
        if(retval instanceof Function) {
            return new RhinoFunctionModel((Function)retval, scriptable, wrapper);
        }
        else {
            return wrapper.wrap(retval);
        }
    }
    
    public TemplateModel get(int index) throws TemplateModelException {
        Object retval = ScriptableObject.getProperty(scriptable, index);
        if(retval instanceof Function) {
            return new RhinoFunctionModel((Function)retval, scriptable, wrapper);
        }
        else {
            return wrapper.wrap(retval);
        }
    }
    
    public boolean isEmpty() {
        return scriptable.getIds().length == 0;
    }
    
    public TemplateCollectionModel keys() throws TemplateModelException {
        return (TemplateCollectionModel)wrapper.wrap(scriptable.getIds());
    }
    
    public int size() {
        return scriptable.getIds().length;
    }
    
    public TemplateCollectionModel values() throws TemplateModelException {
        Object[] ids = scriptable.getIds();
        Object[] values = new Object[ids.length];
        for (int i = 0; i < values.length; i++) {
            Object id = ids[i];
            if(id instanceof Number) {
                values[i] = ScriptableObject.getProperty(scriptable, 
                        ((Number)id).intValue());
            }
            else {
                values[i] = ScriptableObject.getProperty(scriptable, 
                        String.valueOf(id)); 
            }
        }
        return (TemplateCollectionModel)wrapper.wrap(values);
    }
    
    Scriptable getScriptable() {
        return scriptable;
    }

    BeansWrapper getWrapper() {
        return wrapper;
    }
    
    public String getAsString() {
        return Context.toString(scriptable);
    }

    public boolean getAsBoolean() {
        return Context.toBoolean(scriptable);
    }

    public Number getAsNumber() {
        return new Double(Context.toNumber(scriptable));
    }

    public Object getAdaptedObject(Class hint) {
        try {
            return NativeJavaObject.coerceType(hint, scriptable);
        }
        catch(EvaluatorException e) {
            return NativeJavaObject.coerceType(Object.class, scriptable);
        }
    }
}
