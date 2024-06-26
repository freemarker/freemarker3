package freemarker3.cache;

import java.net.URL;

/**
 * A {@link TemplateLoader} that uses streams reachable through 
 * {@link Class#getResourceAsStream(String)} as its source of templates.
 * @author Attila Szegedi, szegedia at freemail dot hu
 * @version $Id: ClassTemplateLoader.java,v 1.11 2005/10/10 21:42:12 ddekany Exp $
 */
public class ClassTemplateLoader extends URLTemplateLoader
{
    private final Class<?> loaderClass;
    private String path;
    
    /**
     * Creates a template loader that will use the 
     * {@link Class#getResource(String)} method of the specified class to load
     * the resources, and the specified base path (absolute or relative).
     *
     * <p>Examples:
     * <ul>
     *   <li>Relative base path (will load from the
     *       <code>com.example.myapplication.templates</code> package):<br>
     *       <code>new ClassTemplateLoader(<br>
     *       com.example.myapplication.SomeClass.class,<br>
     *       "templates")</code>
     *   <li>Absolute base path:<br>
     *       <code>new ClassTemplateLoader(<br>
     *       somepackage.SomeClass.class,<br>
     *       "/com/example/myapplication/templates")</code>
     * </ul>
     *
     * @param loaderClass the class whose {@link Class#getResource(String)} method will be used
     *     to load the templates. Be sure that you chose a class whose defining class-loader
     *     sees the templates. This parameter can't be <code>null</code>.
     * @param path the base path to template resources.
     *     A path that doesn't start with a slash (/) is relative to the
     *     path (package) of the specified class. A path that starts with a slash
     *     is an absolute path starting from the root of the package hierarchy. Path
     *     components should be separated by forward slashes independently of the
     *     separator character used by the underlying operating system.
     *     This parameter can't be <code>null</code>.
     */     
    public ClassTemplateLoader(Class<?> loaderClass, String path) {
        if(path == null)
        {
            throw new IllegalArgumentException("path == null");
        }
        this.loaderClass = loaderClass == null ? this.getClass() : loaderClass;
        this.path = canonicalizePrefix(path);
    }
    


    protected URL getURL(String name)
    {
        return loaderClass.getResource(path + name);
    }
}