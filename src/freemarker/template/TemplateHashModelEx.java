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
 * @see SimpleMapModel
 * @version $Id: TemplateHashModelEx.java,v 1.13 2003/06/08 00:58:15 herbyderby Exp $
 */
public interface TemplateHashModelEx extends TemplateHashModel {

    /**
     * @return the number of key/value mappings in the hash.
     */
    int size();

    /**
     * @return a collection containing the keys in the hash. Every element of 
     * the returned collection must implement the {@link TemplateScalarModel}
     * (as the keys of hashes are always strings).
     */
    TemplateCollectionModel keys();

    /**
     * @return a collection containing the values in the hash.
     */
    TemplateCollectionModel values();
}
