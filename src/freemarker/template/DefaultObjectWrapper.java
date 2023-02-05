package freemarker.template;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import freemarker.ext.dom.NodeModel;

/**
 * <p>The default implementation of the ObjectWrapper
 * interface.
 *
 * @version $Id: DefaultObjectWrapper.java,v 1.24 2005/06/25 16:32:10 revusky Exp $
 */
public class DefaultObjectWrapper extends freemarker.ext.beans.BeansWrapper {
    
    static final DefaultObjectWrapper instance = new DefaultObjectWrapper();
    
    public TemplateModel wrap(Object obj) {
        if (obj == null) {
            return super.wrap(null);
        }
        if (obj instanceof TemplateModel) {
            return (TemplateModel) obj;
        }
        if (obj instanceof String) {
            return new SimpleScalar((String) obj);
        }
        if (obj instanceof Number) {
            return new SimpleNumber((Number) obj);
        }
        if (obj instanceof java.util.Date) {
            if(obj instanceof java.sql.Date) {
                return new SimpleDate((java.sql.Date) obj);
            }
            if(obj instanceof java.sql.Time) {
                return new SimpleDate((java.sql.Time) obj);
            }
            if(obj instanceof java.sql.Timestamp) {
                return new SimpleDate((java.sql.Timestamp) obj);
            }
            return new SimpleDate((java.util.Date) obj, getDefaultDateType());
        }
        if (obj.getClass().isArray()) {
            obj = convertArray(obj);
        }
        if (obj instanceof Collection) {
            return new SimpleSequence((Collection) obj, this);
        }
        if (obj instanceof Map) {
            return new SimpleHash((Map) obj, this);
        }
        if (obj instanceof Boolean) {
            return obj.equals(Boolean.TRUE) ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
        }
        if (obj instanceof Iterator) {
            return new SimpleCollection((Iterator) obj, this);
        }
        return handleUnknownType(obj);
    }
    
    
    /**
     * Called if an unknown type is passed in.
     * Since 2.3, this falls back on XML wrapper and BeansWrapper functionality.
     */
    protected TemplateModel handleUnknownType(Object obj) {
        if (obj instanceof org.w3c.dom.Node) {
            return wrapDomNode(obj);
        }
        return super.wrap(obj); 
    }

    
    public TemplateModel wrapDomNode(Object obj) {
        return NodeModel.wrap((org.w3c.dom.Node) obj);
    }

    /**
     * Converts an array to a java.util.List
     */
    protected Object convertArray(Object arr) {
        final int size = Array.getLength(arr);
        ArrayList<Object> list = new ArrayList<>(size);
        for (int i=0;i<size; i++) {
            list.add(Array.get(arr, i));
        }
        return list;
    }
}
