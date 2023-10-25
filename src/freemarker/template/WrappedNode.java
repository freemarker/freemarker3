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

public interface WrappedNode extends WrappedVariable {
    
    /**
     * @return the parent of this node or null, in which case
     * this node is the root of the tree.
     */
    WrappedNode getParentNode() throws EvaluationException;
    
    /**
     * @return a sequence containing this node's children.
     * If the returned value is null or empty, this is essentially 
     * a leaf node.
     */
    WrappedSequence getChildNodes() throws EvaluationException;

    /**
     * @return a String that is used to determine the processing
     * routine to use. In the XML implementation, if the node 
     * is an element, it returns the element's tag name.  If it
     * is an attribute, it returns the attribute's name. It 
     * returns "@text" for text nodes, "@pi" for processing instructions,
     * and so on.
     */    
    String getNodeName() throws EvaluationException;
    
    /**
     * @return a String describing the <em>type</em> of node this is.
     * In the W3C DOM, this should be "element", "text", "attribute", etc.
     * A WrappedNode implementation that models other kinds of
     * trees could return whatever is appropriate for that application. It
     * can be null, if you don't want to use node-types.
     */
    String getNodeType() throws EvaluationException;
    
    
    /**
     * @return the XML namespace URI with which this node is 
     * associated. If this WrappedNode implementation is 
     * not XML-related, it will almost certainly be null. Even 
     * for XML nodes, this will often be null.
     */
    String getNodeNamespace() throws EvaluationException;
}
