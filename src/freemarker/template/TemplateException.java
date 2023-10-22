package freemarker.template;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.UnifiedCall;
import freemarker.core.nodes.generated.IncludeInstruction;
import freemarker.core.nodes.generated.TemplateNode;

/**
 * The FreeMarker classes usually use this exception and its descendants to
 * signal FreeMarker specific exceptions.
 *
 * @version $Id: TemplateException.java,v 1.27 2006/02/12 20:01:45 revusky Exp $
 */
public class TemplateException extends RuntimeException {

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[]{};

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};
    
    /** The underlying cause of this exception, if any */
    private final Environment env;
    
    private List<TemplateNode> ftlStack;


    /**
     * Constructs a TemplateException with no specified detail message
     * or underlying cause.
     */
    public TemplateException(Environment env) {
        this(null, null, env);
    }

    /**
     * Constructs a TemplateException with the given detail message,
     * but no underlying cause exception.
     *
     * @param description the description of the error that occurred
     */
    public TemplateException(String description, Environment env) {
        this(description, null, env);
    }

    /**
     * Constructs a TemplateException with the given underlying Exception,
     * but no detail message.
     *
     * @param cause the underlying <code>Exception</code> that caused this
     * exception to be raised
     */
    public TemplateException(Exception cause, Environment env) {
        this(null, cause, env);
    }

    /**
     * Constructs a TemplateException with both a description of the error
     * that occurred and the underlying Exception that caused this exception
     * to be raised.
     *
     * @param description the description of the error that occurred
     * @param cause the underlying <code>Exception</code> that caused this
     * exception to be raised
     */
    public TemplateException(String description, Exception cause, Environment env) {
        super(getDescription(description, cause), cause);
        this.env = env;
        if(env != null) {
            ftlStack = new ArrayList<TemplateNode>();
            for (TemplateNode location : env.getElementStack()) {
            	ftlStack.add(location);
            }
            Collections.reverse(ftlStack); // We put this in opposite order, as the trace is usually displayed that way.
        }
    }

    private static String getDescription(String description, Exception cause)  {
        if(description != null) {
            return description;
        }
        if(cause != null) {
            return cause.getClass().getName() + ": " + cause.getMessage();
        }
        return "No error message";
    }
    
    /**
     * <p>Returns the underlying exception that caused this exception to be
     * generated.</p>
     * <p><b>Note:</b><br />
     * avoided calling it <code>getCause</code> to avoid name clash with
     * JDK 1.4 method. This would be problematic because the JDK 1.4 method
     * returns a <code>Throwable</code> rather than an <code>Exception</code>.</p>
     *
     * @return the underlying <code>Exception</code>, if any, that caused this
     * exception to be raised
     */
    public Exception getCauseException() {
        return (Exception)getCause();
    }

    /**
     * Returns the quote of the problematic FTL instruction and the FTL stack strace.
     * As of FreeMarker 2.4, we provide access to the FTL instruction stack
     * so you might prefer to use getFTLStack() and format the items in 
     * list yourself.
     * @see #getFTLStack() 
     */
    public String getFTLInstructionStack() {
    	StringBuilder buf = new StringBuilder("----------\n");
    	if (ftlStack != null) {
        	boolean atFirstElement = true;
    		for (TemplateNode location : ftlStack) {
    			if (atFirstElement) {
    				atFirstElement = false;
    	            buf.append("==> ");
    	            buf.append(location.getDescription());
    	            buf.append(" [");
    	            buf.append(location.getLocation());
    	            buf.append("]\n");
    			} else if (location instanceof UnifiedCall || location instanceof IncludeInstruction){ // We only show macro calls and includes
                    String line = location.getDescription() + " ["
                    + location.getLocation() + "]";
                    if (line != null && line.length() > 0) {
                    	buf.append(" in ");
                    	buf.append(line);
                    	buf.append("\n");
                    }
    			}
    		}
        	buf.append("----------\n");
    	}
    	return buf.toString();
    }
    
    /**
     * @return the FTL call stack (starting with current element)
     */
    
    public List<TemplateNode> getFTLStack() {
    	if (ftlStack == null) {
    		return Collections.emptyList();
    	}
    	return Collections.unmodifiableList(ftlStack);
    }

    /**
     * @return the execution environment in which the exception occurred
     */
    public Environment getEnvironment() {
        return env;
    }

    public void printStackTrace(java.io.PrintStream ps) {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(ps), true);
        printStackTrace(pw);
        pw.flush();
    }

    public void printStackTrace(PrintWriter pw) {
        pw.println();
        pw.println(getMessage());
        pw.println(getFTLInstructionStack());
        pw.println("Java backtrace for programmers:");
        pw.println("----------");
        super.printStackTrace(pw);
        
        // Dirty hack to fight with stupid ServletException class whose
        // getCause() method doesn't work properly. Also an aid for pre-J2xE 1.4
        // users.
        try {
            // Reflection is used to prevent dependency on Servlet classes.
            Throwable causeException = getCause();
            Method m = causeException.getClass().getMethod("getRootCause", EMPTY_CLASS_ARRAY);
            Throwable rootCause = (Throwable) m.invoke(causeException, EMPTY_OBJECT_ARRAY);
            if (rootCause != null) {
                Throwable j14Cause = null;
                if (causeException != null) {
                    m = causeException.getClass().getMethod("getCause", EMPTY_CLASS_ARRAY);
                    j14Cause = (Throwable) m.invoke(causeException, EMPTY_OBJECT_ARRAY);
                }
                if (j14Cause == null) {
                    pw.println("ServletException root cause: ");
                    rootCause.printStackTrace(pw);
                }
            }
        } catch (Throwable exc) {
            ; // ignore
        }
    }
}