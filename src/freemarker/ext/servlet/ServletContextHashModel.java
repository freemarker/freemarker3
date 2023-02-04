package freemarker.ext.servlet;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * TemplateHashModel wrapper for a ServletContext attributes.
 * @author Attila Szegedi
 * @version $Id: ServletContextHashModel.java,v 1.12 2003/01/12 23:40:14 revusky Exp $
 */
public final class ServletContextHashModel implements TemplateHashModel
{
    private final GenericServlet servlet;
    private final ServletContext servletctx;
    private final ObjectWrapper wrapper;

    public ServletContextHashModel(
        GenericServlet servlet, ObjectWrapper wrapper)
    {
        this.servlet = servlet;
        this.servletctx = servlet.getServletContext();
        this.wrapper = wrapper;
    }
    
    /**
     * @deprecated use 
     * {@link #ServletContextHashModel(GenericServlet, ObjectWrapper)} instead.
     */
    public ServletContextHashModel(
        ServletContext servletctx, ObjectWrapper wrapper)
    {
        this.servlet = null;
        this.servletctx = servletctx;
        this.wrapper = wrapper;
    }

    public TemplateModel get(String key) throws TemplateModelException
    {
        return wrapper.wrap(servletctx.getAttribute(key));
    }

    public boolean isEmpty()
    {
        return !servletctx.getAttributeNames().hasMoreElements();
    }
    
    /**
     * Returns the underlying servlet. Can return null if this object was
     * created using the deprecated constructor.
     */
    public GenericServlet getServlet()
    {
        return servlet;
    }
}
