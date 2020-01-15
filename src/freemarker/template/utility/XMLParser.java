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
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.ext.dom.NodeModel;
import org.xml.sax.InputSource;

/**
 * A utility class that allows people to instantiate an XML
 * tree directly from within a FreeMarker template.
 * It is both a method and a transform. If you use it 
 * as a transform, the body of the transform is assumed to evaluate
 * to a well-formed XML document. You need to set a parameter in that
 * case of var="someVariableName" so that the root of the document 
 * tree is exposed to the template via that name.
 *
 * If you use it as a method, you just call the method and it takes
 * one parameter, a string containing the XML.
 *
 * @author <mailto:jon@revusky.com>Jonathan Revusky</a>
 */

@Parameters("var")

public class XMLParser implements TemplateTransformModel, TemplateMethodModel
{
    
    private String varname;
    public Writer getWriter(final Writer out,
                            final Map args) throws TemplateModelException
    {
        final StringBuilder buf = new StringBuilder();
        final Environment env = Environment.getCurrentEnvironment();
        TemplateModel name = (TemplateModel) args.get("var");
        if (name == null) {
            throw new TemplateModelException("You must specify a var= parameter to say the name of the variable to assign the XML node to.");
        }
        if (!(name instanceof TemplateScalarModel)) {
            throw new TemplateModelException("The var parameter must be a string.");
        }
        varname = ((TemplateScalarModel) name).getAsString();
        
        return new Writer() {
            public void write(char cbuf[], int off, int len) {
                buf.append(cbuf, off, len);
            }

            public void flush() throws IOException {
            }

            public void close() throws IOException {
                TemplateNodeModel root = null;
                try {
                   root = xmlNodeFromString(buf.toString());
                } catch (Exception e) {
                    if (e instanceof IOException) throw (IOException) e;
                    throw new IOException(e.getMessage());
                }
                env.setVariable(varname, root);
            }
        };
    }
    
    public Object exec(List args) throws TemplateModelException {
        if (args.size() != 1) {
            throw new TemplateModelException("Expecting one argument (a string containing an XML document)");
        }
        try {
            return xmlNodeFromString((String) args.get(0));
        } catch (Exception e) {
            throw new TemplateModelException(e.getMessage());
        }
    }
    
    /**
     * Utility routine to create an FTL node variable from a string.
     */
    static public TemplateNodeModel xmlNodeFromString(String s) throws Exception {
        InputSource is = new InputSource(new StringReader(s));
        return NodeModel.parse(is);
    }
}
