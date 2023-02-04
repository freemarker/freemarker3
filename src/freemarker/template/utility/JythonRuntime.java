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
