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

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.Tag;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class SimpleTagDirectiveModel extends JspTagModelBase<SimpleTag> implements TemplateDirectiveModel
{
    protected SimpleTagDirectiveModel(Class<? extends SimpleTag> tagClass) throws IntrospectionException {
        super(tagClass);
        if(!SimpleTag.class.isAssignableFrom(tagClass)) {
            throw new IllegalArgumentException(tagClass.getName() + 
                    " does not implement either the " + Tag.class.getName() + 
                    " interface or the " + SimpleTag.class.getName() + 
                    " interface.");
        }
    }

    public void execute(Environment env, Map<String, TemplateModel> args, 
            TemplateModel[] outArgs, final TemplateDirectiveBody body) 
    throws TemplateException, IOException {
        try {
            SimpleTag tag = getTagInstance();
            final FreeMarkerPageContext pageContext = PageContextFactory.getCurrentPageContext();
            pageContext.pushWriter(new JspWriterAdapter(env.getOut()));
            try {
                tag.setJspContext(pageContext);
                JspTag parentTag = pageContext.peekTopTag(JspTag.class);
                if(parentTag != null) {
                    tag.setParent(parentTag);
                }
                setupTag(tag, args, pageContext.getObjectWrapper());
                if(body != null) {
                    tag.setJspBody(new JspFragment() {
                        @Override
                        public JspContext getJspContext() {
                            return pageContext;
                        }
                        
                        @Override
                        public void invoke(Writer out) throws JspException, IOException {
                            try {
                                body.render(out == null ? pageContext.getOut() : out);
                            }
                            catch(TemplateException e) {
                                throw new JspException(e);
                            }
                        }
                    });
                    pageContext.pushTopTag(tag);
                    try {
                        tag.doTag();
                    }
                    finally {
                        pageContext.popTopTag();
                    }
                }
                else {
                    tag.doTag();
                }
            }
            finally {
                pageContext.popWriter();
            }
        }
        catch(TemplateException e) {
            throw e;
        }
        catch(RuntimeException e) {
            throw e;
        }
        catch(Exception e) {
            throw new TemplateModelException(e);
        }
    }
}
