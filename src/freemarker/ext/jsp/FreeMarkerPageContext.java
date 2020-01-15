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


package freemarker.ext.jsp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;

import freemarker.core.Environment;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.ServletContextHashModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateScalarModel;
import freemarker.template.utility.DeepUnwrap;
import freemarker.template.utility.UndeclaredThrowableException;

/**
 * @version $Id: FreeMarkerPageContext.java,v 1.27 2005/10/26 17:57:03 revusky Exp $
 * @author Attila Szegedi
 */
abstract class FreeMarkerPageContext extends PageContext implements TemplateModel
{
    private final Environment environment;
    private List tags = new ArrayList();
    private List outs = new ArrayList();
    private final GenericServlet servlet;
    private HttpSession session;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ObjectWrapper wrapper;
    private JspWriter jspOut;
    
    protected FreeMarkerPageContext() throws TemplateModelException
    {
        environment = Environment.getCurrentEnvironment();

        TemplateModel appModel = environment.get (FreemarkerServlet.KEY_APPLICATION_PRIVATE);
        if(!(appModel instanceof ServletContextHashModel)) {
            appModel = environment.get (FreemarkerServlet.KEY_APPLICATION);
        }
        if(appModel instanceof ServletContextHashModel) {
            this.servlet = ((ServletContextHashModel)appModel).getServlet();
        }
        else {
            throw new  TemplateModelException("Could not find an instance of " + 
                    ServletContextHashModel.class.getName() + 
                    " in the data model under either the name " + 
                    FreemarkerServlet.KEY_APPLICATION_PRIVATE + " or " + 
                    FreemarkerServlet.KEY_APPLICATION);
        }
        
        TemplateModel requestModel = 
            environment.get(FreemarkerServlet.KEY_REQUEST_PRIVATE);
        if(!(requestModel instanceof HttpRequestHashModel)) {
            requestModel = environment.get(FreemarkerServlet.KEY_REQUEST);
        }
        if(requestModel instanceof HttpRequestHashModel) {
            HttpRequestHashModel reqHash = (HttpRequestHashModel)requestModel;
            this.request = reqHash.getRequest();
            this.session = request.getSession(false);
            this.response = reqHash.getResponse();
            this.wrapper = reqHash.getObjectWrapper();
        }
        else  {
            throw new  TemplateModelException("Could not find an instance of " + 
                    HttpRequestHashModel.class.getName() + 
                    " in the data model under either the name " + 
                    FreemarkerServlet.KEY_REQUEST_PRIVATE + " or " + 
                    FreemarkerServlet.KEY_REQUEST);
        }

        // Register page attributes as per spec
        setAttribute(REQUEST, request);
        setAttribute(RESPONSE, response);
        if (session != null)
            setAttribute(SESSION, session);
        setAttribute(PAGE, servlet);
        setAttribute(CONFIG, servlet.getServletConfig());
        setAttribute(PAGECONTEXT, this);
        setAttribute(APPLICATION, servlet.getServletContext());
    }    
            
    ObjectWrapper getObjectWrapper() {
        return wrapper;
    }
    
    public void initialize(
        Servlet servlet, ServletRequest request, ServletResponse response,
        String errorPageURL, boolean needsSession, int bufferSize, 
        boolean autoFlush)
    {
        throw new UnsupportedOperationException();
    }

    public void release() {
    }

    public void setAttribute(String name, Object value) {
        setAttribute(name, value, PAGE_SCOPE);
    }

    public void setAttribute(String name, Object value, int scope) {
        switch(scope) {
            case PAGE_SCOPE: {
                try {
                    environment.setGlobalVariable(name, wrapper.wrap(value));
                    break;
                }
                catch(TemplateModelException e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
            case REQUEST_SCOPE: {
                getRequest().setAttribute(name, value);
                break;
            }
            case SESSION_SCOPE: {
                getSession(true).setAttribute(name, value);
                break;
            }
            case APPLICATION_SCOPE: {
                getServletContext().setAttribute(name, value);
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid scope " + scope);
            }
        }
    }

    public Object getAttribute(String name)
    {
        try {
            return DeepUnwrap.permissiveUnwrap(
                    environment.getGlobalNamespace().get(name));
        }
        catch (TemplateModelException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public Object getAttribute(String name, int scope)
    {
        switch (scope) {
            case PAGE_SCOPE: {
                return getAttribute(name);
            }
            case REQUEST_SCOPE: {
                return getRequest().getAttribute(name);
            }
            case SESSION_SCOPE: {
                HttpSession session = getSession(false);
                if(session == null) {
                    return null;
                }
                return session.getAttribute(name);
            }
            case APPLICATION_SCOPE: {
                return getServletContext().getAttribute(name);
            }
            default: {
                throw new IllegalArgumentException("Invalid scope " + scope);
            }
        }
    }

    public Object findAttribute(String name)
    {
        Object retval = getAttribute(name, PAGE_SCOPE);
        if(retval != null) return retval;
        retval = getAttribute(name, REQUEST_SCOPE);
        if(retval != null) return retval;
        retval = getAttribute(name, SESSION_SCOPE);
        if(retval != null) return retval;
        return getAttribute(name, APPLICATION_SCOPE);
    }

    public void removeAttribute(String name) {
        removeAttribute(name, PAGE_SCOPE);
        removeAttribute(name, REQUEST_SCOPE);
        removeAttribute(name, SESSION_SCOPE);
        removeAttribute(name, APPLICATION_SCOPE);
    }

    public void removeAttribute(String name, int scope) {
        switch(scope) {
            case PAGE_SCOPE: {
                environment.getGlobalNamespace().remove(name);
                break;
            }
            case REQUEST_SCOPE: {
                getRequest().removeAttribute(name);
                break;
            }
            case SESSION_SCOPE: {
                HttpSession session = getSession(false);
                if(session != null) {
                    session.removeAttribute(name);
                }
                break;
            }
            case APPLICATION_SCOPE: {
                getServletContext().removeAttribute(name);
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid scope: " + scope);
            }
        }
    }

    public int getAttributesScope(String name) {
        if(getAttribute(name, PAGE_SCOPE) != null) return PAGE_SCOPE;
        if(getAttribute(name, REQUEST_SCOPE) != null) return REQUEST_SCOPE;
        if(getAttribute(name, SESSION_SCOPE) != null) return SESSION_SCOPE;
        if(getAttribute(name, APPLICATION_SCOPE) != null) return APPLICATION_SCOPE;
        return 0;
    }

    public Enumeration getAttributeNamesInScope(int scope) {
        switch(scope) {
            case PAGE_SCOPE: {
                try {
                    return 
                        new TemplateHashModelExEnumeration(environment.getGlobalNamespace());
                }
                catch(TemplateModelException e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
            case REQUEST_SCOPE: {
                return getRequest().getAttributeNames();
            }
            case SESSION_SCOPE: {
                HttpSession session = getSession(false);
                if(session != null) {
                    return session.getAttributeNames();
                }
                return Collections.enumeration(Collections.EMPTY_SET);
            }
            case APPLICATION_SCOPE: {
                return getServletContext().getAttributeNames();
            }
            default: {
                throw new IllegalArgumentException("Invalid scope " + scope);
            }
        }
    }

    public JspWriter getOut() {
        return jspOut;
    }

    private HttpSession getSession(boolean create) {
        if(session == null) {
            session = request.getSession(create);
            if(session != null) {
                setAttribute(SESSION, session);
            }
        }
        return session;
    }

    public HttpSession getSession() {
        return getSession(false);
    }
    
    public Object getPage() {
        return servlet;
    }

    public ServletRequest getRequest() {
        return request;
    }

    public ServletResponse getResponse() {
        return response;
    }

    public Exception getException() {
        throw new UnsupportedOperationException();
    }

    public ServletConfig getServletConfig() {
        return servlet.getServletConfig();
    }

    public ServletContext getServletContext() {
        return servlet.getServletContext();
    }

    public void forward(String url) throws ServletException, IOException {
        //TODO: make sure this is 100% correct by looking at Jasper output 
        request.getRequestDispatcher(url).forward(request, response);
    }

    public void include(String url) throws ServletException, IOException {
        jspOut.flush();
        request.getRequestDispatcher(url).include(request, response);
    }

    public void include(String url, boolean flush) throws ServletException, IOException {
        if(flush) {
            jspOut.flush();
        }
        final PrintWriter pw = new PrintWriter(jspOut);
        request.getRequestDispatcher(url).include(request, new HttpServletResponseWrapper(response) {
            public PrintWriter getWriter() {
                return pw;
            }
            
            public ServletOutputStream getOutputStream() {
                throw new UnsupportedOperationException("JSP-included resource must use getWriter()");
            }
        });
        pw.flush();
    }

    public void handlePageException(Exception e) {
        throw new UnsupportedOperationException();
    }

    public void handlePageException(Throwable e) {
        throw new UnsupportedOperationException();
    }

    public BodyContent pushBody() {
        BodyContent bc = new TagTransformModel.BodyContentImpl(getOut(), true);
        pushWriter(bc);
        return bc;
    }

    public JspWriter popBody() {
        popWriter();
        return (JspWriter) getAttribute(OUT);
    }

    <T> T peekTopTag(Class<T> tagClass) {
        for (ListIterator iter = tags.listIterator(tags.size()); iter.hasPrevious();)
        {
            Object tag = iter.previous();
            if(tagClass.isInstance(tag)) {
                return tagClass.cast(tag);
            }
        }
        return null;
    }  

    void popTopTag() {
        tags.remove(tags.size() - 1);
    }  

    void popWriter() {
        jspOut = (JspWriter)outs.remove(outs.size() - 1);
        setAttribute(OUT, jspOut);
    }
    
    void pushTopTag(Object tag) {
        tags.add(tag);
    } 
    
    void pushWriter(JspWriter out) {
        outs.add(jspOut);
        jspOut = out;
        setAttribute(OUT, jspOut);
    } 
    
    private static class TemplateHashModelExEnumeration implements Enumeration {
        private final TemplateModelIterator it;
            
        private TemplateHashModelExEnumeration(TemplateHashModelEx hashEx) throws TemplateModelException {
            it = hashEx.keys().iterator();
        }
        
        public boolean hasMoreElements() {
            try {
                return it.hasNext();
            } catch (TemplateModelException tme) {
                throw new UndeclaredThrowableException(tme);
            }
        }
        
        public Object nextElement() {
            try {
                return ((TemplateScalarModel) it.next()).getAsString();
            } catch (TemplateModelException tme) {
                throw new UndeclaredThrowableException(tme);
            }
        }
    }
}
