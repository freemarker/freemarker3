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
