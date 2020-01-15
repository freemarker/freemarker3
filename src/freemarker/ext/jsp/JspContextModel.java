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

package freemarker.ext.jsp;

import javax.servlet.jsp.PageContext;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

class JspContextModel
implements
    TemplateHashModel
{
    public static final int ANY_SCOPE = -1;
    public static final int PAGE_SCOPE = PageContext.PAGE_SCOPE;
    public static final int REQUEST_SCOPE = PageContext.REQUEST_SCOPE;
    public static final int SESSION_SCOPE = PageContext.SESSION_SCOPE;
    public static final int APPLICATION_SCOPE = PageContext.APPLICATION_SCOPE;

    private final PageContext pageContext;
    private final int scope;

    public JspContextModel(PageContext pageContext, int scope)
    {
        this.pageContext = pageContext;
        this.scope = scope;
    }

    public TemplateModel get(String key) throws TemplateModelException
    {
        Object bean = scope == ANY_SCOPE ? pageContext.findAttribute(key) : pageContext.getAttribute(key, scope);
        return BeansWrapper.getDefaultInstance().wrap(bean);
    }

    public boolean isEmpty()
    {
        return false;
    }
}
