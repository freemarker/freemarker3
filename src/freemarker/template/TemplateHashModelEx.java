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
 * <p>An extended hash interface with a couple of extra hooks. If a class
 * implements this interface, then the built-in operators <code>?size</code>,
 * <code>?keys</code>, and <code>?values</code> can be applied to its
 * instances in the template.</p>
 *
 * <p>As of version 2.2.2, the engine will automatically wrap the
 * collections returned by <code>keys</code> and <code>values</code> to
 * present them as sequences to the template.  For performance, you may
 * wish to return objects that implement both TemplateCollectionModel
 * and {@link TemplateSequenceModel}. Note that the wrapping to sequence happens
 * on demand; if the template does not try to use the variable returned by
 * <code>?keys</code> or <code>?values</code> as sequence (<code>theKeys?size</code>, or <code>theKeys[x]</code>,
 * or <code>theKeys?sort</code>, etc.), just iterates over the variable
 * (<code>&lt;#list foo?keys as k>...</code>), then no wrapping to
 * sequence will happen, thus there will be no overhead. 
 * 
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 * @see SimpleHash
 * @version $Id: TemplateHashModelEx.java,v 1.13 2003/06/08 00:58:15 herbyderby Exp $
 */
public interface TemplateHashModelEx extends TemplateHashModel {

    /**
     * @return the number of key/value mappings in the hash.
     */
    int size() throws TemplateModelException;

    /**
     * @return a collection containing the keys in the hash. Every element of 
     * the returned collection must implement the {@link TemplateScalarModel}
     * (as the keys of hashes are always strings).
     */
    TemplateCollectionModel keys() throws TemplateModelException;

    /**
     * @return a collection containing the values in the hash.
     */
    TemplateCollectionModel values() throws TemplateModelException;
}
