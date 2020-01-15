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

package freemarker.template;

/**
 * This interface can be implemented by a class to make a variable "foreach-able", 
 * i.e. the model can be used as the list in a &lt;foreach...&gt;
 * or a &lt;list...&gt; directive. Use this model when 
 * your collection does not support index-based access and possibly,
 * the size cannot be known in advance. If you need index-based
 * access, use a {@link TemplateSequenceModel} instead.
 * @see SimpleSequence
 * @see SimpleCollection
 *
 * @author Attila Szegedi, szegedia at users dot sourceforge dot net
 * @version $Id: TemplateCollectionModel.java,v 1.10 2003/01/12 23:40:21 revusky Exp $
 */
public interface TemplateCollectionModel extends TemplateModel {

    /**
     * Retrieves a template model iterator that is used to iterate over
     * the elements in this collection.
     */
    public TemplateModelIterator iterator() throws TemplateModelException;
}
