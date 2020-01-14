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

package freemarker.ext.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * An extension of SimpleHash that looks up keys in the hash, then in the
 * request, session, and servlet context scopes. Makes "Application", "Session"
 * and "Request" keys largely obsolete, however we keep them for backward
 * compatibility (also, "Request" is required for proper operation of JSP
 * taglibs).
 * It is on purpose that we didn't override <tt>keys</tt> and <tt>values</tt>
 * methods. That way, only those variables assigned into the hash directly by a
 * subclass of <tt>FreemarkerServlet</tt> that overrides
 * <tt>preTemplateProcess</tt>) are discovered as "page" variables by the FM
 * JSP PageContext implementation.
 * @author Attila Szegedi
 * @version $Id: AllHttpScopesHashModel.java,v 1.5 2003/01/12 23:40:14 revusky Exp $
 */
public class AllHttpScopesHashModel extends SimpleHash
{
    private static final long serialVersionUID = -3686722132942947752L;

    private final ObjectWrapper wrapper;
    private final ServletContext context;
    private final HttpServletRequest request;
    private final Map unlistedModels = new HashMap();
     
    /**
     * Creates a new instance of AllHttpScopesHashModel for handling a single 
     * HTTP servlet request.
     * @param wrapper the object wrapper to use
     * @param context the servlet context of the web application
     * @param request the HTTP servlet request being processed
     */
    public AllHttpScopesHashModel(ObjectWrapper wrapper, 
            ServletContext context, HttpServletRequest request) {
        this.wrapper = wrapper;
        this.context = context;
        this.request = request;
    }
    
    /**
     * Stores a model in the hash so that it doesn't show up in <tt>keys()</tt>
     * and <tt>values()</tt> methods. Used to put the Application, Session,
     * Request, RequestParameters and JspTaglibs objects.
     * @param key the key under which the model is stored
     * @param model the stored model
     */
    public void putUnlistedModel(String key, TemplateModel model)
    {
        unlistedModels.put(key, model);
    }

    public TemplateModel get(String key) throws TemplateModelException {
        // Lookup in page scope
        TemplateModel model = super.get(key);
        if(model != null) {
            return model;
        }

        // Look in unlisted models
        model = (TemplateModel)unlistedModels.get(key);
        if(model != null) {
            return model;
        }
        
        // Lookup in request scope
        Object obj = request.getAttribute(key);
        if(obj != null) {
            return wrapper.wrap(obj);
        }

        // Lookup in session scope
        HttpSession session = request.getSession(false);
        if(session != null) {
            obj = session.getAttribute(key);
            if(obj != null) {
                return wrapper.wrap(obj);
            }
        }

        // Lookup in application scope
        obj = context.getAttribute(key);
        if(obj != null) {
            return wrapper.wrap(obj);
        }

        return null;
    }
}
