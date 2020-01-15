/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
