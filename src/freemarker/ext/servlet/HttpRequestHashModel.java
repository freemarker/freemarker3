package freemarker.ext.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import freemarker.template.*;

/**
 * TemplateHashModel wrapper for a HttpServletRequest attributes.
 * @author Attila Szegedi
 * @version $Id: HttpRequestHashModel.java,v 1.16 2005/05/05 07:49:58 vsajip Exp $
 */
public final class HttpRequestHashModel implements TemplateHashModelEx
{
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ObjectWrapper wrapper;

    public HttpRequestHashModel(
        HttpServletRequest request, ObjectWrapper wrapper)
    {
        this(request, null, wrapper);
    }

    public HttpRequestHashModel(
        HttpServletRequest request, HttpServletResponse response, 
        ObjectWrapper wrapper)
    {
        this.request = request;
        this.response = response;
        this.wrapper = wrapper;
    }
    
    public TemplateModel get(String key) throws TemplateModelException
    {
        return wrapper.wrap(request.getAttribute(key));
    }

    public boolean isEmpty()
    {
        return !request.getAttributeNames().hasMoreElements();
    }
    
    public int size() {
        int result = 0;
        for (Enumeration enumeration = request.getAttributeNames(); enumeration.hasMoreElements();) {
            enumeration.nextElement();
            ++result;
        }
        return result;
    }
    
    public TemplateCollectionModel keys() {
        ArrayList keys = new ArrayList();
        for (Enumeration enumeration = request.getAttributeNames(); enumeration.hasMoreElements();) {
            keys.add(enumeration.nextElement());
        }
        return new SimpleCollection(keys.iterator());
    }
    
    public TemplateCollectionModel values() {
        ArrayList values = new ArrayList();
        for (Enumeration enumeration = request.getAttributeNames(); enumeration.hasMoreElements();) {
            values.add(request.getAttribute((String)enumeration.nextElement()));
        }
        return new SimpleCollection(values.iterator(), wrapper);
    }

    public HttpServletRequest getRequest()
    {
        return request;
    }
    
    public HttpServletResponse getResponse()
    {
        return response;
    }
    
    public ObjectWrapper getObjectWrapper()
    {
        return wrapper;
    }
}
