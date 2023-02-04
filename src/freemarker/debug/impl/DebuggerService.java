package freemarker.debug.impl;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import freemarker.core.Environment;
import freemarker.template.Template;
import freemarker.template.utility.SecurityUtilities;

/**
 * This class provides debugging hooks for the core FreeMarker engine. It is
 * not usable for anyone outside the FreeMarker core classes. It is public only
 * as an implementation detail.
 * @author Attila Szegedi
 * @version $Id: DebuggerService.java,v 1.2 2003/05/30 16:51:53 szegedia Exp $
 */
public abstract class DebuggerService
{
    private static final DebuggerService instance = createInstance();
    
    private static DebuggerService createInstance()
    {
        // Creates the appropriate service class. If the debugging is turned
        // off, this is a fast no-op service, otherwise it is the real-thing
        // RMI service.
        return 
            SecurityUtilities.getSystemProperty("freemarker.debug.password") == null
            ? (DebuggerService)new NoOpDebuggerService()
            : (DebuggerService)new RmiDebuggerService();
    }

    public static List getBreakpoints(String templateName)
    {
        return instance.getBreakpointsSpi(templateName);
    }
    
    abstract List getBreakpointsSpi(String templateName);

    public static void registerTemplate(Template template)
    {
        instance.registerTemplateSpi(template);
    }
    
    abstract void registerTemplateSpi(Template template);
    
    public static boolean suspendEnvironment(Environment env, int line)
    throws
        RemoteException
    {
        return instance.suspendEnvironmentSpi(env, line);
    }
    
    abstract boolean suspendEnvironmentSpi(Environment env, int line)
    throws
        RemoteException;

    private static class NoOpDebuggerService extends DebuggerService
    {
        List getBreakpointsSpi(String templateName)
        {
            return Collections.EMPTY_LIST;
        }
        
        boolean suspendEnvironmentSpi(Environment env, int line)
        {
            throw new UnsupportedOperationException();
        }
        
        void registerTemplateSpi(Template template)
        {
        }
    }
}