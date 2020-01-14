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


package freemarker.cache;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.security.CodeSource;

/**
 * This is an abstract template loader that can load templates whose
 * location can be described by an URL. Subclasses only need to override
 * the {@link #getURL(String)} method. Both {@link ClassTemplateLoader} and
 * {@link WebappTemplateLoader} are (quite trivial) subclasses of this class.
 * @version $Id: URLTemplateLoader.java,v 1.14 2003/01/29 08:01:17 szegedia Exp $
 * @author Attila Szegedi
 */
public abstract class URLTemplateLoader implements SecureTemplateLoader
{
    public Object findTemplateSource(String name)
    throws
    	IOException
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
     * For jar: URLs, returns a code source that points to the URL of the JAR
     * file as the code source URL. If the JAR file is signed, the code source
     * will contain the appropriate certificates as well. For other URLs, 
     * returns the code source with URL itself and no certificates.
     * @return an appropriate CodeSource for this template source.
     * @throws IOException
     */
    public CodeSource getCodeSource(Object templateSource) 
    throws
        IOException
    {
        return ((URLTemplateSource)templateSource).getCodeSource();
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
