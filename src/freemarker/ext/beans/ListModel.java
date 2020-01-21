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

import java.util.Collection;
import java.util.List;

import freemarker.ext.util.ModelFactory;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

/**
 * <p>A special case of {@link BeanModel} that can wrap Java lists
 * and that implements the {@link TemplateCollectionModel} and 
 * {@link TemplateSequenceModel} in order to be usable in a <tt>&lt;foreach></tt> block.</p>
 * @author Attila Szegedi
 * @version $Id: CollectionModel.java,v 1.22 2003/06/03 13:21:32 szegedia Exp $
 */
public class ListModel
extends
    CollectionModel
implements
    TemplateSequenceModel
{
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object, ObjectWrapper wrapper)
            {
                return new ListModel((List)object, (BeansWrapper)wrapper);
            }
        };


    /**
     * Creates a new model that wraps the specified collection object.
     * @param list the list object to wrap into a model.
     * @param wrapper the {@link BeansWrapper} associated with this model.
     * Every model has to have an associated {@link BeansWrapper} instance. The
     * model gains many attributes from its wrapper, including the caching 
     * behavior, method exposure level, method-over-item shadowing policy etc.
     */
    public ListModel(List list, BeansWrapper wrapper)
    {
        super(list, wrapper);
    }

    /**
     * Retrieves the i-th object from the collection, wrapped as a TemplateModel.
     * @return null if the index is out of bounds
     * *@throws TemplateModelException if the underlying collection is not a List.
     */
    public TemplateModel get(int index)
    throws
        TemplateModelException
    {
        try
        {
            return wrap(((List)object).get(index));
        }
        catch(IndexOutOfBoundsException e)
        {
            return null; // This is better because it allows the use of existence built-ins, i.e. x[10]?? etcetera (JR)
        }
    }
    
    public int size()
    {
        return ((Collection)object).size();
    }
}
