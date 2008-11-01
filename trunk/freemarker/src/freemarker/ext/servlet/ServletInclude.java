package freemarker.ext.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import freemarker.core.Environment;
import freemarker.template.Parameters;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateScalarModel;


/**
 * A model that when invoked with a 'path' parameter will perform a servlet 
 * include.
 * @author Attila Szegedi
 * @version $Id: $
 */
@Parameters("path")
public class ServletInclude implements TemplateDirectiveModel
{
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    
    public ServletInclude(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }
    
    public void execute(final Environment env, Map<String, TemplateModel> params, 
            TemplateModel[] loopVars, TemplateDirectiveBody body)
    throws TemplateException, IOException
    {
        final TemplateModel path = params.get("path");
        if(path == null) {
            throw new TemplateException("Missing required parameter 'path'", env);
        }
        if(!(path instanceof TemplateScalarModel)) {
            throw new TemplateException("Expected a scalar model. 'path' is instead " + 
                    path.getClass().getName(), env);
        }
        final String strPath = ((TemplateScalarModel)path).getAsString();
        if(strPath == null) {
            throw new TemplateException("String value of 'path' parameter is null", env);
        }
        final Writer envOut = env.getOut(); 
        final HttpServletResponse wrappedResponse;
        if(envOut == response.getWriter()) {
            // Don't bother wrapping if environment's writer is same as 
            // response writer
            wrappedResponse = response;
        }
        else {
            // Otherwise, create a response wrapper that will pass the
            // env writer, potentially first wrapping it in a print
            // writer when it ain't one already.
            wrappedResponse = new HttpServletResponseWrapper(response) {
                @Override
                public PrintWriter getWriter() {
                    return (envOut instanceof PrintWriter) ?
                        (PrintWriter)envOut :
                        new PrintWriter(envOut);
                }
            };
        }
        try {
            request.getRequestDispatcher(strPath).include(request, wrappedResponse);
        }
        catch (ServletException e) {
            throw new TemplateException(e, env);
        }
    }
}
