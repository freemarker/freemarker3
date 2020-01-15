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
 * Describes objects that are nodes in a tree.
 * If you have a tree of objects, they can be recursively
 * <em>visited</em> using the &lt;#visit...&gt; and &lt;#recurse...&gt;
 * FTL directives. This API is largely based on the W3C Document Object Model
 * (DOM) API. However, it is meant to be generally useful for describing
 * any tree of objects that you wish to navigate using a recursive visitor
 * design pattern.
 * @since FreeMarker 2.3
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */

public interface TemplateNodeModel extends TemplateModel {
    
    /**
     * @return the parent of this node or null, in which case
     * this node is the root of the tree.
     */
    TemplateNodeModel getParentNode() throws TemplateModelException;
    
    /**
     * @return a sequence containing this node's children.
     * If the returned value is null or empty, this is essentially 
     * a leaf node.
     */
    TemplateSequenceModel getChildNodes() throws TemplateModelException;

    /**
     * @return a String that is used to determine the processing
     * routine to use. In the XML implementation, if the node 
     * is an element, it returns the element's tag name.  If it
     * is an attribute, it returns the attribute's name. It 
     * returns "@text" for text nodes, "@pi" for processing instructions,
     * and so on.
     */    
    String getNodeName() throws TemplateModelException;
    
    /**
     * @return a String describing the <em>type</em> of node this is.
     * In the W3C DOM, this should be "element", "text", "attribute", etc.
     * A TemplateNodeModel implementation that models other kinds of
     * trees could return whatever is appropriate for that application. It
     * can be null, if you don't want to use node-types.
     */
    String getNodeType() throws TemplateModelException;
    
    
    /**
     * @return the XML namespace URI with which this node is 
     * associated. If this TemplateNodeModel implementation is 
     * not XML-related, it will almost certainly be null. Even 
     * for XML nodes, this will often be null.
     */
    String getNodeNamespace() throws TemplateModelException;
}
