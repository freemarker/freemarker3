package freemarker.debug;

import java.io.Serializable;

/**
 * Represents a breakpoint location consisting of a template name and a line number.
 * @author Attila Szegedi
 * @version $Id: Breakpoint.java,v 1.1 2003/05/02 15:55:47 szegedia Exp $
 */
public class Breakpoint implements Serializable, Comparable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3052047457697630123L;
	private final String templateName;
    private final int line;
    
    /**
     * Creates a new breakpoint.
     * @param templateName the name of the template
     * @param line the line number in the template where to put the breakpoint
     */
    public Breakpoint(String templateName, int line)
    {
        this.templateName = templateName;
        this.line = line;
    }

    /**
     * Returns the line number of the breakpoint
     */
    public int getLine()
    {
        return line;
    }
    /**
     * Returns the template name of the breakpoint
     */
    public String getTemplateName()
    {
        return templateName;
    }

    public int hashCode()
    {
        return templateName.hashCode() + 31 * line;
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof Breakpoint)
        {
            Breakpoint b = (Breakpoint)o;
            return b.templateName.equals(templateName) && b.line == line;
        }
        return false;
    }
    
    public int compareTo(Object o)
    {
        Breakpoint b = (Breakpoint)o;
        int r = templateName.compareTo(b.templateName);
        return r == 0 ? line - b.line : r;
    }
    
    /**
     * Returns the template name and the line number separated with a colon
     */
    public String getLocationString()
    {
        return templateName + ":" + line;
    }
}
