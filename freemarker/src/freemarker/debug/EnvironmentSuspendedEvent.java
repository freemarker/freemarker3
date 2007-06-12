package freemarker.debug;

import java.util.EventObject;

/**
 * Event describing a suspension of an environment (ie because it hit a
 * breakpoint).
 * @author Attila Szegedi
 * @version $Id: EnvironmentSuspendedEvent.java,v 1.1 2003/05/02 15:55:48 szegedia Exp $
 */
public class EnvironmentSuspendedEvent extends EventObject
{
    private final int line;
    private final DebuggedEnvironment env;

    public EnvironmentSuspendedEvent(Object source, int line, DebuggedEnvironment env)
    {
        super(source);
        this.line = line;
        this.env = env;
    }

    /**
     * The line number in the template where the execution of the environment
     * was suspended.
     * @return int the line number
     */
    public int getLine()
    {
        return line;
    }

    /**
     * The environment that was suspended
     * @return DebuggedEnvironment
     */
    public DebuggedEnvironment getEnvironment()
    {
        return env;
    }
}
