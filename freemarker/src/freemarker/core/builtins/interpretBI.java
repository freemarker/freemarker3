/*
 * Copyright (c) 2007 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.core.builtins;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.Expression;
import freemarker.template.*;

/**
 * Implementation of ?new built-in 
 */

public class interpretBI extends BuiltIn {
	
	public TemplateModel get(TemplateModel target, String builtInName, Environment env, BuiltInExpression callingExpression) throws TemplateException {
		String id = null, interpretString = null;
		if (target instanceof TemplateSequenceModel) {
			TemplateSequenceModel tsm = (TemplateSequenceModel) target;
			TemplateModel tm = tsm.get(1);
			if (tm != null && !(tm instanceof TemplateScalarModel)) {
				throw new TemplateModelException("Expecting string as second item of sequence of left of ?interpret built-in");
			} else if (tm != null){
				id = ((TemplateScalarModel) tm).getAsString();
			}
			tm = tsm.get(0);
			if (!(tm instanceof TemplateScalarModel)) {
				throw new TemplateModelException("Expecting string as first item of sequence of left of ?interpret built-in");
			}
			interpretString = ((TemplateScalarModel) tm).getAsString();
		}
		else if (target instanceof TemplateScalarModel) {
			interpretString = ((TemplateScalarModel) target).getAsString();
		}
		if (id == null) id = "anonymous_interpreted";
		if (interpretString == null) {
			throw new InvalidReferenceException("No string to interpret", env);
		}
        Template parentTemplate = env.getTemplate();
        try {
            Template template = new Template(parentTemplate.getName() + "$" + id, new StringReader(interpretString), parentTemplate.getConfiguration());
            template.setLocale(env.getLocale());
            return new TemplateProcessorModel(template);
        }
        catch(IOException e)
        {
            throw new TemplateException("", e, env);
        }
	}
	

    private static class TemplateProcessorModel
    implements
        TemplateTransformModel
    {
        private final Template template;
        
        TemplateProcessorModel(Template template)
        {
            this.template = template;
        }
        
        public Writer getWriter(final Writer out, Map args) throws TemplateModelException, IOException
        {
            try
            {
                Environment env = Environment.getCurrentEnvironment();
                env.include(template);
            }
            catch(TemplateModelException e)
            {
                throw e;
            }
            catch(IOException e)
            {
                throw e;
            }
            catch(RuntimeException e)
            {
                throw e;
            }
            catch(Exception e)
            {
                throw new TemplateModelException(e);
            }
    
            return new Writer(out)
            {
                public void close()
                {
                }
                
                public void flush() throws IOException
                {
                    out.flush();
                }
                
                public void write(char[] cbuf, int off, int len) throws IOException
                {
                    out.write(cbuf, off, len);
                }
            };
        }
    }
}
