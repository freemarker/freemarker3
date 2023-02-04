package freemarker.ext.servlet;

import freemarker.template.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * TemplateHashModel wrapper for a HttpServletRequest parameters.
 * @author Attila Szegedi
 * @version $Id: HttpRequestParametersHashModel.java,v 1.21 2005/05/05 07:50:25 vsajip Exp $
 */

public class HttpRequestParametersHashModel
    implements
    TemplateHashModelEx
{
    private final HttpServletRequest request;
    private List keys;
        
    public HttpRequestParametersHashModel(HttpServletRequest request)
    {
        this.request = request;
    }

    public TemplateModel get(String key)
    {
        String value = request.getParameter(key);
        return value == null ? null : new SimpleScalar(value);
    }

    public boolean isEmpty()
    {
        return !request.getParameterNames().hasMoreElements();
    }
    
    public int size() {
        return getKeys().size();
    }
    
    public TemplateCollectionModel keys() {
        return new SimpleCollection(getKeys().iterator());
    }
    
    public TemplateCollectionModel values() {
        final Iterator iter = getKeys().iterator();
        return new SimpleCollection(
            new Iterator() {
                public boolean hasNext() {
                    return iter.hasNext();
                }
                public Object next() {
                    return request.getParameter((String)iter.next()); 
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            });
    }

    protected String transcode(String string)
    {
        return string;
    }

    private synchronized List getKeys() {
        if(keys == null) {
            keys = new ArrayList();
            for (Enumeration enumeration = request.getParameterNames(); enumeration.hasMoreElements();) {
                keys.add(enumeration.nextElement());
            }
        }
        return keys;
    }
}
