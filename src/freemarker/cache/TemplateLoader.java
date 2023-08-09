package freemarker.cache;

import java.io.IOException;
import java.io.Reader;

/**
 * A template loader is an object that can find the source stream for a 
 * template, can retrieve its time of last modification as well as the stream
 * itself. A template loader is plugged into the {@link TemplateCache} to
 * provide concrete loading of the templates.
 * The implementations can be coded in a non-threadsafe manner as the natural
 * user of the template loader, {@link TemplateCache} does the necessary
 * synchronization.
 * @author Attila Szegedi, szegedia at freemail dot hu
 * @version $Id: TemplateLoader.java,v 1.18 2004/03/01 01:13:30 ddekany Exp $
 */
public interface TemplateLoader
{
    /**
     * Finds the object that acts as the source of the template with the
     * given name. This method is called by the TemplateCache when a template
     * is requested, before calling either {@link #getLastModified(Object)} or
     * {@link #getReader(Object, String)}.
     *
     * @param name the name of the template, already localized and normalized by
     * the {@link freemarker.cache.TemplateCache cache}.
     * It is completely up to the loader implementation to interpret
     * the name, however it should expect to receive hierarchical paths where
     * path components are separated by a slash (not backslash). Backslashes
     * (or any other OS specific separator character) are not considered as separators by
     * FreeMarker, and thus they will not be replaced with slash before passing to this method,
     * so it is up to the template loader to handle them (say, be throwing and exception that
     * tells the user that the path (s)he has entered is invalid, as (s)he must use slash --
     * typical mistake of Windows users).
     * The passed names are always considered relative to some loader-defined root
     * location (often reffered as the "template root direcotry"), and will never start with
     * a slash, nor will they contain a path component consisting of either a single or a double
     * dot -- these are all resolved by the template cache before passing the name to the
     * loader. As a side effect, paths that trivially reach outside template root directory,
     * such as <tt>../my.ftl</tt>, will be rejected by the template cache, so they never
     * reach the template loader. Note again, that if the path uses backslash as path separator
     * instead of slash as (the template loader should not accept that), the normalisation will
     * not properly happen, as FreeMarker (the cache) recognizes only the slashes as separators.
     *
     * @return an object representing the template source, which can be
     * supplied in subsequent calls to {@link #getLastModified(Object)} and
     * {@link #getReader(Object, String)}. Null must be returned if the source
     * for the template can not be found (do not throw <code>FileNotFoundException</code>!).
     * The returned object may will be compared with a cached template source
     * object for equality, using the <code>equals</code> method. Thus,
     * objects returned for the same physical source must be equivalent
     * according to <code>equals</code> method, otherwise template caching
     * can become very ineffective!
     * @throws IOException if there is an I/O exception while looking for the
     * template source
     */
    public Object findTemplateSource(String name) throws IOException;
        
    /**
     * Returns the time of last modification of the specified template source.
     * This method is called after <code>findTemplateSource()</code>.
     * @param templateSource an object representing a template source, obtained
     * through a prior call to {@link #findTemplateSource(String)}.
     * @return the time of last modification of the specified template source,
     * or -1 if the time is not known.
     */
    public long getLastModified(Object templateSource);
    
    /**
     * Returns the character stream of a template represented by the specified
     * template source. This method is called after <code>getLastModified()</code>
     * if it is determined that a cached copy of the template is unavailable
     * or stale.
     * @param templateSource an object representing a template source, obtained
     * through a prior call to {@link #findTemplateSource(String)}.
     * @param encoding the character encoding used to translate source bytes
     * to characters. Some loaders may not have access to the byte
     * representation of the template stream, and instead directly obtain a 
     * character stream. These loaders will - quite naturally - ignore the 
     * encoding parameter.
     * @return a reader representing the template character stream. The
     * framework will call <code>close()</code>.
     * @throws IOException if an I/O error occurs while accessing the stream.
     */
    public Reader getReader(Object templateSource, String encoding) throws IOException;
    
    /**
     * Closes the template source. This is the last method that is called by
     * the TemplateCache for a templateSource. The framework guarantees that
     * this method will be called on every object that is returned from
     * {@link #findTemplateSource(String)}.
     * @param templateSource the template source that should be closed.
     * @throws IOException if there is an I/O exception while closing the 
     * template source
     */
    public void closeTemplateSource(Object templateSource) throws IOException;
}
