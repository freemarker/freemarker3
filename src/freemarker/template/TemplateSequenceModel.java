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
 * List values in a template data model whose elements are accessed by the 
 * index operator should implement this interface. In addition to
 * accessing elements by index and querying size using the <code>?size</code>
 * built-in, objects that implement this interface can be iterated in 
 * <code>&lt;#foreach ...></code> and <code>&lt;#list ...></code> directives. The 
 * iteration is implemented by calling the {@link #get(int)} method 
 * repeatedly starting from zero and going to <tt>{@link #size()} - 1</tt>.
 * @author Attila Szegedi, szegedia at users dot sourceforge dot net
 * @version $Id: TemplateSequenceModel.java,v 1.10 2004/11/27 14:49:57 ddekany Exp $
 */
public interface TemplateSequenceModel extends TemplateModel {

    /**
     * Retrieves the i-th template model in this sequence.
     * 
     * @return the item at the specified index, or <code>null</code> if
     * the index is out of bounds. Note that a <code>null</code> value is
     * interpreted by FreeMarker as "variable does not exist", and accessing
     * a missing variables is usually considered as an error in the FreeMarker
     * Template Language, so the usage of a bad index will not remain hidden.
     */
    TemplateModel get(int index) throws TemplateModelException;

    /**
     * @return the number of items in the list.
     */
    int size() throws TemplateModelException;
}
