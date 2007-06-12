package freemarker.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.util.Map;
import java.util.WeakHashMap;

import freemarker.core.ast.TemplateElement;
import freemarker.template.TemplateException;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public abstract class SecureRenderer
{
    private static final byte[] secureInvokerImplBytecode = loadBytecode();
    
    // We're storing a CodeSource -> (ClassLoader -> SecureRenderer), since we
    // need to have one renderer per class loader. We're using weak hash maps
    // and soft references all the way, since we don't want to interfere with
    // cleanup of either CodeSource or ClassLoader objects.
    private static final Map<CodeSource, Map<ClassLoader, SoftReference<SecureRenderer>>> renderers = 
        new WeakHashMap<CodeSource, Map<ClassLoader, SoftReference<SecureRenderer>>>();
    
    public abstract void render(Environment e, TemplateElement t)
    throws TemplateException, IOException;
    
    /**
     * Render the specified template element with the specified environment,
     * using a protection domain belonging to the specified code source. 
     * @param codeSource the code source defining the protection domain
     * @param env the processing environment
     * @param t the processed template element
     * @throws TemplateException if environment's processing of the template
     * element throws the exception
     * @throws IOException if environment's processing of the template
     * element throws the exception
     */
    static void renderSecurely(final CodeSource codeSource, Environment env, 
            TemplateElement t)
    throws TemplateException, IOException
    {
        final Thread thread = Thread.currentThread();
        // Run in doPrivileged as we might be checked for "getClassLoader" 
        // runtime permission
        final ClassLoader classLoader = AccessController.doPrivileged(
            new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return thread.getContextClassLoader();
                }
            });
        Map<ClassLoader, SoftReference<SecureRenderer>> classLoaderMap;
        synchronized(renderers)
        {
            classLoaderMap = renderers.get(codeSource);
            if(classLoaderMap == null)
            {
                classLoaderMap = new WeakHashMap<ClassLoader, SoftReference<SecureRenderer>>();
                renderers.put(codeSource, classLoaderMap);
            }
        }
        SecureRenderer renderer;
        synchronized(classLoaderMap)
        {
            SoftReference<SecureRenderer> ref = classLoaderMap.get(classLoader);
            if(ref != null)
            {
                renderer = ref.get();
            }
            else
            {
                renderer = null;
            }
            if(renderer == null)
            {
                try
                {
                    // Run in doPrivileged as we'll be checked for 
                    // "createClassLoader" runtime permission
                    renderer = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<SecureRenderer>()
                    {
                        public SecureRenderer run() throws Exception
                        {
                            ClassLoader effectiveClassLoader;
                            Class thisClass = getClass();
                            if(classLoader.loadClass(thisClass.getName()) != thisClass) {
                                effectiveClassLoader = thisClass.getClassLoader();
                            } else {
                                effectiveClassLoader = classLoader;
                            }  
                            SecureClassLoaderImpl secCl = 
                                new SecureClassLoaderImpl(effectiveClassLoader);
                            Class c = secCl.defineAndLinkClass(
                                    SecureRenderer.class.getName() + "Impl", 
                                    secureInvokerImplBytecode, codeSource);
                            return (SecureRenderer)c.newInstance();
                        }
                    });
                    classLoaderMap.put(classLoader, 
                            new SoftReference<SecureRenderer>(renderer));
                }
                catch(PrivilegedActionException ex)
                {
                    throw new UndeclaredThrowableException(ex.getCause());
                }
            }
        }
        renderer.render(env, t);
    }
    
    private static class SecureClassLoaderImpl extends SecureClassLoader
    {
        SecureClassLoaderImpl(ClassLoader parent)
        {
            super(parent);
        }
        
        Class defineAndLinkClass(String name, byte[] bytes, CodeSource cs)
        {
            Class cl = defineClass(name, bytes, 0, bytes.length, cs);
            resolveClass(cl);
            return cl;
        }
    }
    
    private static byte[] loadBytecode()
    {
        return AccessController.doPrivileged(new PrivilegedAction<byte[]>()
        {
            public byte[] run()
            {
                return loadBytecodePrivileged();
            }
        });
    }
    
    private static byte[] loadBytecodePrivileged()
    {
        URL url = SecureRenderer.class.getResource("SecureRendererImpl.clazz");
        try
        {
            InputStream in = url.openStream();
            try
            {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                for(;;)
                {
                    int r = in.read();
                    if(r == -1)
                    {
                        return bout.toByteArray();
                    }
                    bout.write(r);
                }
            }
            finally
            {
                in.close();
            }
        }
        catch(IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }
}
