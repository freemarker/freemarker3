package freemarker.debug;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

/**
 * The main debugger interface. Allows management of breakpoints as well as
 * installation of listeners for debug events.
 * @author Attila Szegedi
 * @version $Id: Debugger.java,v 1.1 2003/05/02 15:55:48 szegedia Exp $
 */
public interface Debugger extends Remote
{
    public static final int DEFAULT_PORT = 7011;

    /**
     * Adds a breakpoint
     * @param breakpoint the breakpoint to add
     * @throws RemoteException
     */
    public void addBreakpoint(Breakpoint breakpoint)
    throws
        RemoteException;
    
    /**
     * Removes a single breakpoint
     * @param breakpoint the breakpoint to remove
     * @throws RemoteException
     */
    public void removeBreakpoint(Breakpoint breakpoint)
    throws
        RemoteException;

    /**
     * Removes all breakpoints for a specific template
     * @param templateName
     * @throws RemoteException
     */
    public void removeBreakpoints(String templateName)
    throws
        RemoteException;

    /**
     * Removes all breakpoints
     * @throws RemoteException
     */
    public void removeBreakpoints()
    throws
        RemoteException;

    /**
     * Retrieves a list of all {@link Breakpoint} objects.
     * @throws RemoteException
     */
    public List getBreakpoints()
    throws
        RemoteException;
        
    /**
     * Retrieves a list of all {@link Breakpoint} objects for the specified
     * template.
     * @throws RemoteException
     */
    public List getBreakpoints(String templateName)
    throws
        RemoteException;

    /**
     * Retrieves a collection of all {@link DebuggedEnvironment} objects that 
     * are currently suspended.
     * @throws RemoteException
     */
    public Collection getSuspendedEnvironments()
    throws
        RemoteException;
        
    /**
     * Adds a listener for debugger events.
     * @return an identification token that should be passed to 
     * {@link #removeDebuggerListener(Object)} to remove this listener.
     * @throws RemoteException
     */
    public Object addDebuggerListener(DebuggerListener listener)
    throws
        RemoteException;
        
    /**
     * Removes a previously added debugger listener.
     * @param id the identification token for the listener that was returned
     * from a prior call to {@link #addDebuggerListener(DebuggerListener)}.
     * @throws RemoteException
     */
    public void removeDebuggerListener(Object id)
    throws
        RemoteException;
}
