/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
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
