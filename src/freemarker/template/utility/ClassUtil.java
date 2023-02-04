package freemarker.template.utility;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author Attila Szegedi
 * @version $Id: ClassUtil.java,v 1.1 2003/03/06 13:16:31 szegedia Exp $
 */
public class ClassUtil
{
    private ClassUtil()
    {
    }
    
    /**
     * Similar to {@link Class#forName(java.lang.String)}, but attempts to load
     * through the thread context class loader. Only if thread context class
     * loader is inaccessible, or it can't find the class will it attempt to
     * fall back to the class loader that loads the FreeMarker classes.
     */
    public static Class<?> forName(String className)
    throws
        ClassNotFoundException
    {
        try
        {
            return Class.forName(className, true, AccessController.doPrivileged(
                    new PrivilegedAction<ClassLoader>() {
                        public ClassLoader run() {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    }));
        }
        catch(ClassNotFoundException e)
        {
            ;// Intentionally ignored
        }
        catch(SecurityException e)
        {
            ;// Intentionally ignored
        }
        // Fall back to default class loader 
        return Class.forName(className);
    }
}
