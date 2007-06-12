/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.ext.jsp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import freemarker.template.SimpleHash;
import freemarker.template.Template;

/**
 * Simple implementation of JSP tag to allow use of FreeMarker templates in
 * JSP. Inspired by similar class in Velocity template engine developed by
 * <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author Attila Szegedi
 */
public class FreemarkerTag implements BodyTag
{
    private Tag parent;
    private BodyContent bodyContent;
    private PageContext pageContext;
    private SimpleHash root;
    private Template template;
    private boolean caching = true;
    private String name = "";
    
    public boolean getCaching()
    {
        return caching;
    }

    public void setCaching(boolean caching)
    {
        this.caching = caching;
    }

    public void setName(String name)
    {
        this.name = name == null ? "" : name;
    }
    
    public Tag getParent()
    {
        return parent;
    }

    public void setParent(Tag parent)
    {
        this.parent = parent;
    }

    public int doStartTag()
    {
        return EVAL_BODY_BUFFERED;
    }

    public void setBodyContent(BodyContent bodyContent)
    {
        this.bodyContent = bodyContent;
    }

    public void setPageContext(PageContext pageContext)
    {
        this.pageContext = pageContext;
        root = null;
    }

    public void doInitBody()
    {
    }

    public int doAfterBody()
    {
        return SKIP_BODY;
    }

    public void release()
    {
        root = null;
        template = null;
        name = "";
    }

    public int doEndTag()
        throws JspException
    {
        if (bodyContent == null)
            return EVAL_PAGE;

        try
        {
            if(template == null)
            {
                template = new Template(name, bodyContent.getReader());
            }

            if(root == null)
            {
                root = new SimpleHash();
                root.put("page", new JspContextModel(pageContext, JspContextModel.PAGE_SCOPE));
                root.put("request", new JspContextModel(pageContext, JspContextModel.REQUEST_SCOPE));
                root.put("session", new JspContextModel(pageContext, JspContextModel.SESSION_SCOPE));
                root.put("application", new JspContextModel(pageContext, JspContextModel.APPLICATION_SCOPE));
                root.put("any", new JspContextModel(pageContext, JspContextModel.ANY_SCOPE));
            }
            template.process(root, pageContext.getOut());
        }
        catch(Exception e)
        {
            try
            {
                pageContext.handlePageException(e);
            }
            catch(ServletException e2)
            {
                throw new JspException(e2.getMessage());
            }
            catch(IOException e2)
            {
                throw new JspException(e2.getMessage());
            }
        }
        finally
        {
            if(!caching)
            {
                template = null;
            }
        }

        return EVAL_PAGE;
    }
}
