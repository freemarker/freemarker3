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
