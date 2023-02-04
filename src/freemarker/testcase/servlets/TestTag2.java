package freemarker.testcase.servlets;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @version $Id: TestTag2.java,v 1.4 2003/01/12 23:40:26 revusky Exp $
 * @author Attila Szegedi
 */
public class TestTag2 extends TagSupport
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -4123183184113269523L;

	public int doStartTag() throws JspException {
        try {
            pageContext.getOut().println("TestTag2.doStartTag() called here");
            return Tag.EVAL_BODY_INCLUDE;
        }
        catch(IOException e) {
            throw new JspException(e);
        }
    }

    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().println("TestTag2.doEndTag() called here");
            return Tag.EVAL_PAGE;
        }
        catch(IOException e) {
            throw new JspException(e);
        }
    }
}
