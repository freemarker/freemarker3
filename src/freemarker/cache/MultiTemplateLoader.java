package freemarker.cache;

import java.io.IOException;
import java.io.Reader;
import java.security.CodeSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link TemplateLoader} that uses a set of other loaders to load the templates.
 * On every request, loaders are queried in the order of their appearance in the
 * array of loaders that this Loader owns. If a request for some template name 
 * was already satisfied in the past by one of the loaders, that Loader is queried 
 * first (a soft affinity).
 * This class is <em>NOT</em> thread-safe. If it is accessed from multiple
 * threads concurrently, proper synchronization must be provided by the callers.
 * Note that {@link TemplateCache}, the natural user of this class provides the
 * necessary synchronizations when it uses the class.
 * @author Attila Szegedi, szegedia at freemail dot hu
 * @version $Id: MultiTemplateLoader.java,v 1.12 2003/08/08 10:10:58 szegedia Exp $
 */
public class MultiTemplateLoader 
implements StatefulTemplateLoader, SecureTemplateLoader
{
    private final TemplateLoader[] loaders;
    private final Map<String, TemplateLoader> lastLoaderForName = 
    	Collections.synchronizedMap(new HashMap<String, TemplateLoader>());
    
    /**
     * Creates a new multi template Loader that will use the specified loaders.
     * @param loaders the loaders that are used to load templates. 
     */
    public MultiTemplateLoader(TemplateLoader... loaders)
    {
        this.loaders = loaders.clone();
    }
    
    public Object findTemplateSource(String name)
    throws
    	IOException
    {
        // Use soft affinity - give the loader that last found this
        // resource a chance to find it again first.
        TemplateLoader lastLoader = lastLoaderForName.get(name);
        if(lastLoader != null)
        {
            Object source = lastLoader.findTemplateSource(name);
            if(source != null)
            {
                return new MultiSource(source, lastLoader);
            }
        }
        
        // If there is no affine loader, or it could not find the resource
        // again, try all loaders in order of appearance. If any manages
        // to find the resource, then associate it as the new affine loader 
        // for this resource.
        for(int i = 0; i < loaders.length; ++i)
        {
            TemplateLoader loader = loaders[i];
            Object source = loader.findTemplateSource(name);
            if(source != null)
            {
                lastLoaderForName.put(name, loader);
                return new MultiSource(source, loader);
            }
        }
        
        lastLoaderForName.remove(name);
        // Resource not found
        return null;
    }
    
    public long getLastModified(Object templateSource)
    {
        return ((MultiSource)templateSource).getLastModified();
    }
    
    public Reader getReader(Object templateSource, String encoding)
    throws
        IOException
    {
        return ((MultiSource)templateSource).getReader(encoding);
    }
    
    public void closeTemplateSource(Object templateSource)
    throws
        IOException
    {
        ((MultiSource)templateSource).close();
    }
    
    public CodeSource getCodeSource(Object templateSource) throws IOException
    {
        return ((MultiSource)templateSource).getCodeSource();
    }

    public void resetState()
    {
        lastLoaderForName.clear();
        for (int i = 0; i < loaders.length; i++) {
            TemplateLoader loader = loaders[i];
            if(loader instanceof StatefulTemplateLoader) {
                ((StatefulTemplateLoader)loader).resetState();
            }
        }
    }

    /**
     * Represents a template source bound to a specific template loader. It
     * serves as the complete template source descriptor used by the
     * MultiTemplateLoader class.
     */
    private static final class MultiSource
    {
        private final Object source;
        private final TemplateLoader loader;
        
        MultiSource(Object source, TemplateLoader loader)
        {
            this.source = source;
            this.loader = loader;
        }
        
        long getLastModified()
        {
            return loader.getLastModified(source);
        }
        
        Reader getReader(String encoding)
        throws
            IOException
        {
            return loader.getReader(source, encoding);
        }
        
        void close()
        throws
            IOException
        {
            loader.closeTemplateSource(source);
        }
        
        CodeSource getCodeSource() throws IOException {
            if(loader instanceof SecureTemplateLoader) {
                return ((SecureTemplateLoader)loader).getCodeSource(source);
            }
            return null;
        }
        
        public boolean equals(Object o) {
            if(o instanceof MultiSource) {
                MultiSource m = (MultiSource)o;
                return m.loader.equals(loader) && m.source.equals(source);
            }
            return false;
        }
        
        public int hashCode() {
            return loader.hashCode() + 31 * source.hashCode();
        }
        
        public String toString() {
            return source.toString();
        }
    }
}
