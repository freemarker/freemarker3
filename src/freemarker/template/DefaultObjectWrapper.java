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
    
    static private Class JYTHON_OBJ_CLASS,
                         RHINO_SCRIPTABLE_CLASS,
                         JRUBY_OBJ_CLASS; 
    
    static private ObjectWrapper JYTHON_WRAPPER, JRUBY_WRAPPER, RHINO_WRAPPER;

    /**
     * @return whether successful
     */
    static boolean enableJython() {
        if (JYTHON_WRAPPER == null) {
            try {
                JYTHON_OBJ_CLASS = Class.forName("org.python.core.PyObject");
                Class clazz = Class.forName("freemarker.ext.jython.JythonWrapper");
                JYTHON_WRAPPER = (ObjectWrapper) clazz.newInstance();
            } catch (Exception e) {
            }
        }
        return JYTHON_WRAPPER != null;
    }

    static boolean enableJRuby() {
        if (JRUBY_WRAPPER == null) {
            try {
                JRUBY_OBJ_CLASS = Class.forName("org.jruby.RubyObject");
                Class clazz = Class.forName("freemarker.ext.jruby.JRubyWrapper");
                JRUBY_WRAPPER = (ObjectWrapper) clazz.newInstance();
            } catch (Exception e) {
            }
        }
        return JRUBY_WRAPPER !=null;
    }
    
    static boolean enableRhino() {
        if (RHINO_WRAPPER == null) {
            try {
                RHINO_SCRIPTABLE_CLASS = Class.forName("org.mozilla.javascript.Scriptable");
                Class clazz = Class.forName("freemarker.ext.rhino.RhinoWrapper");
                RHINO_WRAPPER = (ObjectWrapper) clazz.newInstance();
            } catch (Exception e) {
            }
        }
        return RHINO_WRAPPER != null;
    }
    

    public TemplateModel wrap(Object obj) throws TemplateModelException {
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
    protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException {
        if (obj instanceof org.w3c.dom.Node) {
            return wrapDomNode(obj);
        }
        if (JYTHON_WRAPPER != null  && JYTHON_OBJ_CLASS.isInstance(obj)) {
            return JYTHON_WRAPPER.wrap(obj);
        }
        if (JRUBY_WRAPPER != null  && JRUBY_OBJ_CLASS.isInstance(obj)) {
            return JRUBY_WRAPPER.wrap(obj);
        }
        if (RHINO_SCRIPTABLE_CLASS != null && RHINO_SCRIPTABLE_CLASS.isInstance(obj)) {
        	return RHINO_WRAPPER.wrap(obj);
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
        ArrayList list = new ArrayList(size);
        for (int i=0;i<size; i++) {
            list.add(Array.get(arr, i));
        }
        return list;
    }
}
