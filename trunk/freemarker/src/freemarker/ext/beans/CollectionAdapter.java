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

import java.util.AbstractCollection;
import java.util.Iterator;

import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelAdapter;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.utility.UndeclaredThrowableException;

/**
 * @author Attila Szegedi
 * @version $Id: CollectionAdapter.java,v 1.2 2005/06/12 19:03:04 szegedia Exp $
 */
class CollectionAdapter extends AbstractCollection implements TemplateModelAdapter {
    private final BeansWrapper wrapper;
    private final TemplateCollectionModel model;
    
    CollectionAdapter(TemplateCollectionModel model, BeansWrapper wrapper) {
        this.model = model;
        this.wrapper = wrapper;
    }
    
    public TemplateModel getTemplateModel() {
        return model;
    }
    
    public int size() {
        throw new UnsupportedOperationException();
    }

    public Iterator iterator() {
        try {
            return new Iterator() {
                final TemplateModelIterator i = model.iterator();
    
                public boolean hasNext() {
                    try {
                        return i.hasNext();
                    }
                    catch(TemplateModelException e) {
                        throw new UndeclaredThrowableException(e);
                    }
                }
                
                public Object next() {
                    try {
                        return wrapper.unwrap(i.next());
                    }
                    catch(TemplateModelException e) {
                        throw new UndeclaredThrowableException(e);
                    }
                }
                
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        catch(TemplateModelException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
