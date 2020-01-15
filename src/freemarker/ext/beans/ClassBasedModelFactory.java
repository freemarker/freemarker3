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
