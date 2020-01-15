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

import java.io.Writer;
import java.io.PrintWriter;

import freemarker.core.Environment;

/**
 * An API for objects that handle exceptions that are thrown during
 * template rendering.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */

public interface TemplateExceptionHandler {
	
	/**
	  * handle the exception.
	  * @param te the exception that occurred.
	  * @param env The environment object that represents the rendering context
	  * @param out the character output stream to output to.
	  */
	void handleTemplateException(TemplateException te, Environment env, Writer out) 
	    throws TemplateException;
            
            
         /**
           * This is a TemplateExceptionHandler which simply skips errors. It does nothing
           * to handle the event. Note that the exception is still logged in any case, before
           * being passed to the handler.
           */
	TemplateExceptionHandler IGNORE_HANDLER = new TemplateExceptionHandler() {
		public void handleTemplateException(TemplateException te, Environment env, Writer out) {
		}
	};
        
         /**
           * This is a TemplateExceptionHandler that simply rethrows the exception.
           * Note that the exception is logged before being rethrown.
           */
	TemplateExceptionHandler RETHROW_HANDLER =new TemplateExceptionHandler() {
		public void handleTemplateException(TemplateException te, Environment env, Writer out) 
                    throws TemplateException  
                {
                    throw te;
		}
	};
        
        /**
          * This is a TemplateExceptionHandler used when you develop the templates. This handler
          * outputs the stack trace information to the client and then rethrows the exception.
          */
	TemplateExceptionHandler DEBUG_HANDLER =new TemplateExceptionHandler() {
		public void handleTemplateException(TemplateException te, Environment env, Writer out) 
                    throws TemplateException  
                {
                    PrintWriter pw = (out instanceof PrintWriter) 
                                 ? (PrintWriter) out 
                                 : new PrintWriter(out);
                    te.printStackTrace(pw);
                    pw.flush();
                    throw te;
		}
	}; 

        /**
          * This is a TemplateExceptionHandler used when you develop HTML templates. This handler
          * outputs the stack trace information to the client and then rethrows the exception, and
          * surrounds it with tags to make the error message readable with the browser.
          */
	TemplateExceptionHandler HTML_DEBUG_HANDLER =new TemplateExceptionHandler() {
		public void handleTemplateException(TemplateException te, Environment env, Writer out) 
                    throws TemplateException  
                {
                    PrintWriter pw = (out instanceof PrintWriter) 
                                 ? (PrintWriter) out 
                                 : new PrintWriter(out);
                    pw.println("<!-- FREEMARKER ERROR MESSAGE STARTS HERE -->"
                            + "<script language=javascript>//\"></script>"
                            + "<script language=javascript>//\'></script>"
                            + "<script language=javascript>//\"></script>"
                            + "<script language=javascript>//\'></script>"
                            + "</title></xmp></script></noscript></style></object>"
                            + "</head></pre></table>"
                            + "</form></table></table></table></a></u></i></b>"
                            + "<div align=left "
                            + "style='background-color:#FFFF00; color:#FF0000; "
                            + "display:block; border-top:double; padding:2pt; "
                            + "font-size:medium; font-family:Arial,sans-serif; "
                            + "font-style: normal; font-variant: normal; "
                            + "font-weight: normal; text-decoration: none; "
                            + "text-transform: none'>"
                            + "<b style='font-size:medium'>FreeMarker template error!</b>"
                            + "<pre><xmp>");
                    te.printStackTrace(pw);
                    pw.println("</xmp></pre></div></html>");
                    pw.flush();
                    throw te;
		}
	}; 
}
