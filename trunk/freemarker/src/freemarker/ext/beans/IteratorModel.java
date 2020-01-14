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

import java.util.Iterator;
import java.util.NoSuchElementException;

import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;

/**
 * <p>A class that adds {@link TemplateModelIterator} functionality to the
 * {@link Iterator} interface implementers. 
 * </p>
 * <p>It differs from the {@link freemarker.template.SimpleCollection} in that 
 * it inherits from {@link BeanModel}, and therefore you can call methods on 
 * it directly, even to the effect of calling <tt>iterator.remove()</tt> in 
 * the template.</p> <p>Using the model as a collection model is NOT 
 * thread-safe, as iterators are inherently not thread-safe.
 * Further, you can iterate over it only once. Attempts to call the
 * {@link #iterator()} method after it was already driven to the end once will 
 * throw an exception.</p>
 * @author Attila Szegedi
 * @version $Id: IteratorModel.java,v 1.26 2003/06/03 13:21:32 szegedia Exp $
 */

public class IteratorModel
extends
    BeanModel
implements
    TemplateModelIterator,
    TemplateCollectionModel
{
    private boolean accessed = false;
    
    /**
     * Creates a new model that wraps the specified iterator object.
     * @param iterator the iterator object to wrap into a model.
     * @param wrapper the {@link BeansWrapper} associated with this model.
     * Every model has to have an associated {@link BeansWrapper} instance. The
     * model gains many attributes from its wrapper, including the caching 
     * behavior, method exposure level, method-over-item shadowing policy etc.
     */
    public IteratorModel(Iterator iterator, BeansWrapper wrapper)
    {
        super(iterator, wrapper);
    }

    /**
     * This allows the iterator to be used in a <tt>&lt;foreach></tt> block.
     * @return "this"
     */
    public TemplateModelIterator iterator() throws TemplateModelException
    {
        synchronized(this) {
            if(accessed) {
                throw new TemplateModelException(
                    "This collection is stateful and can not be iterated over the" +
                    " second time.");
            }
            accessed = true;
        }
        return this;
    }
    
    /**
     * Calls underlying {@link Iterator#hasNext()}.
     */
    public boolean hasNext() {
        return ((Iterator)object).hasNext();
    }


    /**
     * Calls underlying {@link Iterator#next()} and wraps the result.
     */
    public TemplateModel next()
    throws
        TemplateModelException
    {
        try {
            return wrap(((Iterator)object).next());
        }
        catch(NoSuchElementException e) {
            throw new TemplateModelException(
                "No more elements in the iterator.", e);
        }
    }

    /**
     * Returns {@link Iterator#hasNext()}. Therefore, an
     * iterator that has no more element evaluates to false, and an 
     * iterator that has further elements evaluates to true.
     */
    public boolean getAsBoolean() {
        return hasNext();
    }
}
