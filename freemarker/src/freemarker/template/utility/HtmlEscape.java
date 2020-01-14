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

package freemarker.template.utility;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import freemarker.template.*;
import freemarker.core.Environment;

/**
 * Performs an HTML escape of a given template fragment. Specifically,
 * &lt; &gt; &quot; and &amp; are all turned into entities.
 *
 * <p>Usage:<br />
 * From java:</p>
 * <pre>
 * SimpleHash root = new SimpleHash();
 *
 * root.put( "htmlEscape", new freemarker.template.utility.HtmlEscape() );
 *
 * ...
 * </pre>
 *
 * <p>From your FreeMarker template:</p>
 * <pre>
 *
 * The following is HTML-escaped:
 * &lt;transform htmlEscape&gt;
 *   &lt;p&gt;This paragraph has all HTML special characters escaped.&lt;/p&gt;
 * &lt;/transform&gt;
 *
 * ...
 * </pre>
 *
 * @version $Id: HtmlEscape.java,v 1.29 2003/02/25 00:28:16 revusky Exp $
 * @see freemarker.template.utility.XmlEscape
 */
public class HtmlEscape implements TemplateTransformModel, TemplateDirectiveModel {

    private static final char[] LT = "&lt;".toCharArray();
    private static final char[] GT = "&gt;".toCharArray();
    private static final char[] AMP = "&amp;".toCharArray();
    private static final char[] QUOT = "&quot;".toCharArray();
    
    public void execute(Environment env, Map<String, TemplateModel> args, TemplateModel[] bodyVars, TemplateDirectiveBody body) throws IOException, TemplateException {
    	body.render(getWriter(env.getOut(), args));
    }

    public Writer getWriter(Writer out, Map args)
    {
        return new HtmlEscapeWriter(out);
    }
    
    class HtmlEscapeWriter extends Writer {
    	
    	Writer out;
    	
    	HtmlEscapeWriter(Writer out){
    		this.out = out;
    	}
    	
        @Override
        public void write(int c) throws IOException
        {
            switch(c)
            {
                case '<': out.write(LT, 0, 4); break;
                case '>': out.write(GT, 0, 4); break;
                case '&': out.write(AMP, 0, 5); break;
                case '"': out.write(QUOT, 0, 6); break;
                default: out.write(c);
            }
        }

        @Override
        public void write(char cbuf[], int off, int len)
        throws
            IOException
        {
            int lastoff = off;
            int lastpos = off + len;
            for (int i = off; i < lastpos; i++)
            {
                switch (cbuf[i])
                {
                    case '<': out.write(cbuf, lastoff, i - lastoff); out.write(LT, 0, 4); lastoff = i + 1; break;
                    case '>': out.write(cbuf, lastoff, i - lastoff); out.write(GT, 0, 4); lastoff = i + 1; break;
                    case '&': out.write(cbuf, lastoff, i - lastoff); out.write(AMP, 0, 5); lastoff = i + 1; break;
                    case '"': out.write(cbuf, lastoff, i - lastoff); out.write(QUOT, 0, 6); lastoff = i + 1; break;
                }
            }
            int remaining = lastpos - lastoff;
            if(remaining > 0)
            {
                out.write(cbuf, lastoff, remaining);
            }
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void close() {
        }
    }
    
}
