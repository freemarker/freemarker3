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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import freemarker.core.Environment;
import freemarker.template.Parameters;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.utility.DeepUnwrap;


/**
 * A model that when invoked with a 'path' parameter will perform a servlet 
 * include. It also support an optional hash named 'params' which specifies
 * request parameters for the include - its keys are strings, its values
 * should be either strings or sequences of strings (for multiple valued 
 * parameters). A third optional parameter 'inherit_params' should be a boolean
 * when specified, and it defaults to true when not specified. A value of true
 * means that the include inherits the request parameters from the current 
 * request. In this case values in 'params' will get prepended to the existing
 * values of parameters.
 * @author Attila Szegedi
 * @version $Id: $
 */
@Parameters("path params=null inherit_params=true")
public class IncludePage implements TemplateDirectiveModel
{
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    
    public IncludePage(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }
    
    public void execute(final Environment env, Map<String, TemplateModel> params, 
            TemplateModel[] loopVars, TemplateDirectiveBody body)
    throws TemplateException, IOException
    {
        // Determine the path
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
        
        // See whether we need to use a custom response (if we're inside a TTM
        // or TDM or macro nested body, we'll need to as then the current 
        // FM environment writer is not identical to HTTP servlet response 
        // writer. 
        final Writer envOut = env.getOut(); 
        final HttpServletResponse wrappedResponse;
        if(envOut == response.getWriter()) {
            // Don't bother wrapping if environment's writer is same as 
            // response writer
            wrappedResponse = response;
        }
        else {
            final PrintWriter printWriter = (envOut instanceof PrintWriter) ?
                (PrintWriter)envOut :
                new PrintWriter(envOut); 
            // Otherwise, create a response wrapper that will pass the
            // env writer, potentially first wrapping it in a print
            // writer when it ain't one already.
            wrappedResponse = new HttpServletResponseWrapper(response) {
                @Override
                public PrintWriter getWriter() {
                    return printWriter;
                }
            };
        }

        // Determine inherit_params value
        final boolean inheritParams;
        final TemplateModel inheritParamsModel = params.get("inherit_params");
        if(inheritParamsModel == null) {
            // defaults to true when not specified
            inheritParams = true; 
        }
        else {
            if(!(inheritParamsModel instanceof TemplateBooleanModel)) {
                throw new TemplateException("'inherit_params' should be a boolean but it is " +
                        inheritParamsModel.getClass().getName() + " instead", env);
            }
            inheritParams = ((TemplateBooleanModel)inheritParamsModel).getAsBoolean();
        }
        
        // Get explicit params, if any
        final TemplateModel paramsModel = params.get("params");
        
        // Determine whether we need to wrap the request
        final HttpServletRequest wrappedRequest;
        if(paramsModel == null && inheritParams) {
            // Inherit original request params & no params explicitly 
            // specified, so use the original request
            wrappedRequest = request;
        }
        else {
            // In any other case, use a custom request wrapper
            final Map paramsMap;
            if(paramsModel != null) {
                // Convert params to a Map
                final Object unwrapped = DeepUnwrap.unwrap(paramsModel);
                if(!(unwrapped instanceof Map)) {
                    throw new TemplateException("Expected 'params' to unwrap " +
                            "into a java.util.Map. It unwrapped into " + 
                            unwrapped.getClass().getName() + " instead.", env);
                }
                paramsMap = (Map)unwrapped;
            }
            else {
                paramsMap = Collections.EMPTY_MAP;
            }
            wrappedRequest = new CustomParamsRequest(request, paramsMap, 
                    inheritParams);
        }
        
        // Finally, do the include
        try {
            request.getRequestDispatcher(strPath).include(wrappedRequest, 
                    wrappedResponse);
        }
        catch (ServletException e) {
            throw new TemplateException(e, env);
        }
    }

    private static final class CustomParamsRequest extends HttpServletRequestWrapper
    {
        private final LinkedHashMap<String, String[]> paramsMap;

        private CustomParamsRequest(HttpServletRequest request, 
                Map<?,?> paramMap, boolean inheritParams) {
            super(request);
            paramsMap = inheritParams ? new LinkedHashMap<String, String[]>(
                    request.getParameterMap()) : new LinkedHashMap<String, String[]>();
            for (Map.Entry<?, ?> entry : paramMap.entrySet()) {
                String name = String.valueOf(entry.getKey());
                Object value = entry.getValue();
                final String[] valueArray;
                if(value == null) {
                    // Null values are explicitly added (so, among other 
                    // things, we can hide inherited param values).
                    valueArray = new String[] { null };
                }
                else if(value instanceof String[]) {
                    // String[] arrays are just passed through
                    valueArray = (String[])value;
                }
                else if(value instanceof Collection) {
                    // Collections are converted to String[], with 
                    // String.valueOf() used on elements
                    Collection col = (Collection)value;
                    valueArray = new String[col.size()];
                    int i = 0;
                    for (Object object : col) {
                        valueArray[i++] = String.valueOf(object);
                    }
                }
                else if(value.getClass().isArray()) {
                    // Other array types are too converted to String[], with 
                    // String.valueOf() used on elements
                    int len = Array.getLength(value);
                    valueArray = new String[len];
                    for(int i = 0; i < len; ++i) {
                        valueArray[i] = String.valueOf(Array.get(value, i));
                    }
                }
                else {
                    // All other values (including strings) are converted to a
                    // single-element String[], with String.valueOf applied to
                    // the value.
                    valueArray = new String[] { String.valueOf(value) };
                }
                String[] existingParams = paramsMap.get(name);
                int el = existingParams == null ? 0 : existingParams.length;
                if(el == 0)
                {
                    paramsMap.put(name, valueArray);
                }
                else
                {
                    int vl = valueArray.length;
                    if(vl > 0)
                    {
                        String[] newValueArray = new String[el + vl];
                        System.arraycopy(valueArray, 0, newValueArray, 0, vl);
                        System.arraycopy(existingParams, 0, newValueArray, vl, el);
                        paramsMap.put(name, newValueArray);
                    }
                }
            }
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] value = paramsMap.get(name);
            return value != null ? value.clone() : null;
        }

        @Override
        public String getParameter(String name) {
            String[] values = paramsMap.get(name);
            return values != null && values.length > 0 ? values[0] : null;
        }

        @Override
        public Enumeration getParameterNames() {
            return Collections.enumeration(paramsMap.keySet());
        }

        @Override
        public Map getParameterMap() {
            LinkedHashMap<String, String[]> clone = (LinkedHashMap)paramsMap.clone();
            for (Map.Entry<String, String[]> entry : clone.entrySet()) {
                entry.setValue(entry.getValue().clone());
            }
            return Collections.unmodifiableMap(clone);
        }
    }
}