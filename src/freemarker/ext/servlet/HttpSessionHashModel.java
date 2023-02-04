package freemarker.ext.servlet;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * TemplateHashModel wrapper for a HttpSession attributes.
 * @author Attila Szegedi
 * @version $Id: HttpSessionHashModel.java,v 1.14 2003/01/12 23:40:14 revusky Exp $
 */

public final class HttpSessionHashModel
implements
	TemplateHashModel, Serializable
{
    private static final long serialVersionUID = 1L;

    private transient HttpSession session;
    private transient final ObjectWrapper wrapper;

    // These are required for lazy initializing session
    private transient final FreemarkerServlet servlet;
    private transient final HttpServletRequest request;
    private transient final HttpServletResponse response;
    
    /**
     * Use this constructor when the session already exists.
     * @param session the session
     * @param wrapper an object wrapper used to wrap session attributes
     */
    public HttpSessionHashModel(HttpSession session, ObjectWrapper wrapper)
    {
        this.session = session;
        this.wrapper = wrapper;

        this.servlet = null;
        this.request = null;
        this.response = null;
    }

    /**
     * Use this constructor when the session isn't already created. It is passed
     * enough parameters so that the session can be properly initialized after
     * it is detected that it was created.
     * @param servlet the FreemarkerServlet that created this model. If the
     * model is not created through FreemarkerServlet, leave this argument as
     * null.
     * @param request the actual request
     * @param response the actual response
     * @param wrapper an object wrapper used to wrap session attributes
     */
    public HttpSessionHashModel(FreemarkerServlet servlet, HttpServletRequest request, HttpServletResponse response, ObjectWrapper wrapper)
    {
        this.wrapper = wrapper;
        
        this.servlet = servlet;
        this.request = request;
        this.response = response;
    }

    public TemplateModel get(String key) throws TemplateModelException
    {
        checkSessionExistence();
        return wrapper.wrap(session != null ? session.getAttribute(key) : null);
    }

    boolean isOrphaned(HttpSession currentSession)
    {
        return (session != null && session != currentSession) || 
            (session == null && request == null);
    }
    
    private void checkSessionExistence() throws TemplateModelException
    {
        if(session == null && request != null) {
            session = request.getSession(false);
            if(session != null && servlet != null) {
                try {
                    servlet.initializeSessionAndInstallModel(request, response, 
                            this, session);
                }
                catch(RuntimeException e) {
                    throw e;
                }
                catch(Exception e) {
                    throw new TemplateModelException(e);
                }
            }
        }
    }

    public boolean isEmpty() throws TemplateModelException
    {
        checkSessionExistence();
        return session == null || !session.getAttributeNames().hasMoreElements();
    }
}
