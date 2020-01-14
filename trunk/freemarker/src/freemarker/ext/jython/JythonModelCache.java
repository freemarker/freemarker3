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

package freemarker.ext.jython;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyJavaInstance;
import org.python.core.PyLong;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyStringMap;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.DateModel;
import freemarker.ext.util.ModelCache;
import freemarker.template.TemplateModel;

class JythonModelCache extends ModelCache
{
    private final JythonWrapper wrapper;
    
    JythonModelCache(JythonWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    protected boolean isCacheable(Object object) {
        return true;
    }
    
    @Override
    protected TemplateModel create(Object obj) {
        boolean asHash = false;
        boolean asSequence = false;
        if(obj instanceof PyJavaInstance) {
            Object jobj = ((PyJavaInstance)obj).__tojava__(java.lang.Object.class);
            // FreeMarker-aware, Jython-wrapped Java objects are left intact 
            if(jobj instanceof TemplateModel) {
                return (TemplateModel)jobj; 
            }
            if(jobj instanceof Map) {
                asHash = true;
            }
            if (jobj instanceof Date) {
                return new DateModel((Date) jobj, BeansWrapper.getDefaultInstance());
            }
            else if(jobj instanceof Collection) {
                asSequence = true;
                // FIXME: This is an ugly hack, but AFAIK, there's no better
                // solution if we want to have Sets and other non-List
                // collections managed by this layer, as Jython quite clearly
                // doesn't support sets.  
                if(!(jobj instanceof List)) {
                    obj = new ArrayList((Collection)jobj); 
                }
            }
        }
        
        // If it's not a PyObject, first make a PyObject out of it.
        if(!(obj instanceof PyObject)) {
            obj = Py.java2py(obj);
        }
        if(asHash || obj instanceof PyDictionary || obj instanceof PyStringMap) {
            return JythonHashModel.FACTORY.create(obj, wrapper);
        }
        if(asSequence || obj instanceof PySequence) {
            return JythonSequenceModel.FACTORY.create(obj, wrapper);
        }
        if(obj instanceof PyInteger || obj instanceof PyLong || obj instanceof PyFloat) {
            return JythonNumberModel.FACTORY.create(obj, wrapper);
        }
        if(obj instanceof PyNone) {
            return TemplateModel.JAVA_NULL;
        }
        return JythonModel.FACTORY.create(obj, wrapper);
    }
}
