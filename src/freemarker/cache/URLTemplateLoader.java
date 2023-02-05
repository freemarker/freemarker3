package freemarker.cache;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * This is an abstract template loader that can load templates whose
 * location can be described by an URL. Subclasses only need to override
 * the {@link #getURL(String)} method. Both {@link ClassTemplateLoader} and
 * {@link WebappTemplateLoader} are (quite trivial) subclasses of this class.
 * @version $Id: URLTemplateLoader.java,v 1.14 2003/01/29 08:01:17 szegedia Exp $
 * @author Attila Szegedi
 */
public abstract class URLTemplateLoader implements TemplateLoader {
    public Object findTemplateSource(String name) throws IOException
    {
        URL url = getURL(name);
        return url == null ? null : new URLTemplateSource(url);
    }
    
    /**
     * Given a template name (plus potential locale decorations) retrieves
     * an URL that points the template source.
     * @param name the name of the sought template, including the locale
     * decorations.
     * @return an URL that points to the template source, or null if it can
     * determine that the template source does not exist.
     */
    protected abstract URL getURL(String name);
    
    public long getLastModified(Object templateSource)
    {
        return ((URLTemplateSource) templateSource).lastModified();
    }
    
    public Reader getReader(Object templateSource, String encoding)
    throws
        IOException
    {
        return new InputStreamReader(
                ((URLTemplateSource) templateSource).getInputStream(),
                encoding);
    }
    
    public void closeTemplateSource(Object templateSource)
    throws
    	IOException
    {
        ((URLTemplateSource) templateSource).close();
    }
    
    /**
     * Can be used by subclasses to canonicalize URL path prefixes.
     * @param prefix the path prefix to canonicalize
     * @return the canonicalized prefix. All backslashes are replaced with
     * forward slashes, and a trailing slash is appended if the original
     * prefix wasn't empty and didn't already end with a slash.
     */
    protected static String canonicalizePrefix(String prefix)
    {
        // make it foolproof
        prefix = prefix.replace('\\', '/');
        // ensure there's a trailing slash
        if (prefix.length() > 0 && !prefix.endsWith("/"))
        {
            prefix += "/";
        }
        return prefix;
    }
}
