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
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import freemarker.template.*;
import freemarker.core.Environment;

/**
 * A crude first pass at an embeddable Jython interpreter
 * @author <mailto:jon@revusky.com>Jonathan Revusky</a>
 */

public class JythonRuntime extends PythonInterpreter
    implements TemplateDirectiveModel
{
	public void execute(Environment env, Map<String, TemplateModel> args, TemplateModel[] loopVars, TemplateDirectiveBody body) 
	throws TemplateException, IOException 
	{
		Writer jythonWriter = new JythonWriter(env.getOut());
		try {
		    body.render(jythonWriter);
		}
		finally {
		    jythonWriter.close();
		}
	}
	
	
    public Writer getWriter(final Writer out,
                            final Map args)
    {
        return new JythonWriter(out);    
    }
    
    class JythonWriter extends Writer {
    	
    	Environment env;
    	StringBuilder buf;
    	Writer out;
    	
    	JythonWriter(Writer out) {
    		this.out = out;
    		this.env = Environment.getCurrentEnvironment();
    		this.buf = new StringBuilder();
    	}
    	
        public void write(char cbuf[], int off, int len) {
            buf.append(cbuf, off, len);
        }

        public void flush() throws IOException {
            interpretBuffer();
            out.flush();
        }

        public void close() {
            interpretBuffer();
        }

        private void interpretBuffer() {
        	Environment env = Environment.getCurrentEnvironment();
            synchronized(JythonRuntime.this) {
                PyObject prevOut = systemState.stdout;
                try {
                    setOut(out);
                    set("env", env);
                    exec(buf.toString());
                    buf.setLength(0);
                } finally {
                    setOut(prevOut);
                }
            }
        }
    }
}
