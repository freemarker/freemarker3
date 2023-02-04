package freemarker.testcase.servlets;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TryCatchFinally;

/**
 * @version $Id: TestTag.java,v 1.6 2003/01/12 23:40:26 revusky Exp $
 * @author Attila Szegedi
 */
public class TestTag extends BodyTagSupport implements TryCatchFinally
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -4284914801318941990L;
	private boolean throwException;
    private int repeatCount;
    
    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    public int doStartTag() throws JspException {
        try {
            pageContext.getOut().println("doStartTag() called here");
            if(throwException) {
                throw new JspException("throwException==true");
            }
            return repeatCount == 0 ? Tag.SKIP_BODY : BodyTag.EVAL_BODY_BUFFERED;
        }
        catch(IOException e) {
            throw new JspException(e);
        }
    }

    public int doAfterBody() throws JspException {
        try {
            getPreviousOut().println("doAfterBody() called here");
            getBodyContent().writeOut(getPreviousOut());
            getBodyContent().clear();
            return --repeatCount == 0 ? Tag.SKIP_BODY : IterationTag.EVAL_BODY_AGAIN;
        }
        catch(IOException e) {
            throw new JspException(e);
        }
    }
    
    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().println("doEndTag() called here");
            return Tag.EVAL_PAGE;
        }
        catch(IOException e) {
            throw new JspException(e);
        }
    }
    
    public void doCatch(Throwable t) throws Throwable {
        pageContext.getOut().println("doCatch() called here with " + t.getClass() + ": " + t.getMessage());
    }

    public void doFinally() {
        try {
            pageContext.getOut().println("doFinally() called here");
        }
        catch(IOException e) {
            throw new Error(); // Shouldn't happen
        }
    }
}
