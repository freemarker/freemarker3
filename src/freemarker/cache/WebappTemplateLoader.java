package freemarker.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.cert.Certificate;

import javax.servlet.ServletContext;

import freemarker.log.Logger;

/**
 * A {@link TemplateLoader} that uses streams reachable through 
 * {@link ServletContext#getResource(String)} as its source of templates.
 * @version $Id: WebappTemplateLoader.java,v 1.10 2003/01/29 08:01:18 szegedia Exp $
 * @author Attila Szegedi
 */
public class WebappTemplateLoader implements SecureTemplateLoader 
{
    private static final Logger logger = Logger.getLogger("freemarker.cache");
    
    private final ServletContext servletContext;
    private final String path;
    
    /**
     * Creates a resource template cache that will use the specified servlet
     * context to load the resources. It will use the base path of 
     * <code>"/"</code> meaning templates will be resolved relative to the 
     * servlet context root location.
     * @param servletContext the servlet context whose
     * {@link ServletContext#getResource(String)} will be used to load the
     * templates.
     */
    public WebappTemplateLoader(ServletContext servletContext) {
        this(servletContext, "/");
    }

    /**
     * Creates a template loader that will use the specified servlet
     * context to load the resources. It will use the specified base path.
     * The is interpreted as relative to the current context root (does not mater
     * if you start it with "/" or not). Path components
     * should be separated by forward slashes independently of the separator 
     * character used by the underlying operating system.
     * @param servletContext the servlet context whose
     * {@link ServletContext#getResource(String)} will be used to load the
     * templates.
     * @param path the base path to template resources.
     */
    public WebappTemplateLoader(ServletContext servletContext, String path) {
        if(servletContext == null) {
            throw new IllegalArgumentException("servletContext == null");
        }
        if(path == null) {
            throw new IllegalArgumentException("path == null");
        }
        
        path = path.replace('\\', '/');
        if(!path.endsWith("/")) {
            path += "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        this.path = path;
        this.servletContext = servletContext;
    }

    public Object findTemplateSource(String name) throws IOException {
        String fullPath = path + name;
        // First try to open as plain file (to bypass servlet container resource caches).
        try {
            String realPath = servletContext.getRealPath(fullPath);
            if (realPath != null) {
                File file = new File(realPath);
                if(!file.isFile()) {
                    return null;
                }
                if(file.canRead()) {                    
                    return file;
                }
            }
        } catch (SecurityException e) {
            ;// ignore
        }
            
        // If it fails, try to open it with servletContext.getResource.
        URL url = null;
        try {
            url = servletContext.getResource(fullPath);
        } catch(MalformedURLException e) {
            logger.warn("Could not retrieve resource " + fullPath, e);
            return null;
        }
        return url == null ? null : new URLTemplateSource(url);
    }
    
    public long getLastModified(Object templateSource) {
        if (templateSource instanceof File) {
            return ((File) templateSource).lastModified();
        } else {
            return ((URLTemplateSource) templateSource).lastModified();
        }
    }
    
    public Reader getReader(Object templateSource, String encoding)
    throws IOException {
        if (templateSource instanceof File) {
            return new InputStreamReader(
                    new FileInputStream((File) templateSource),
                    encoding);
        } else {
            return new InputStreamReader(
                    ((URLTemplateSource) templateSource).getInputStream(),
                    encoding);
        }
    }
    
    public void closeTemplateSource(Object templateSource) throws IOException {
        if (templateSource instanceof File) {
            // Do nothing.
        } else {
            ((URLTemplateSource) templateSource).close();
        }
    }
    
    public CodeSource getCodeSource(Object templateSource) throws IOException {
        if(templateSource instanceof File) {
            return new CodeSource(((File)templateSource).toURL(), (Certificate[])null);
        }
        return ((URLTemplateSource)templateSource).getCodeSource();
    }
}