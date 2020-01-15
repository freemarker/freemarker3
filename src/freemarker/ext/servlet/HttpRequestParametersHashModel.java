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
