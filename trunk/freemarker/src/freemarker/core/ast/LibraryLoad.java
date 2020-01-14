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

package freemarker.core.ast;

import java.io.IOException;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.parser.ParseException;



/**
 * An instruction that gets another template
 * and processes it within the current template.
 */
public final class LibraryLoad extends TemplateElement {

    private Expression templateName;
    private String namespace;
    private String templatePath="";

    /**
     * @param template the template that this <tt>Include</tt> is a part of.
     * @param templateName the name of the template to be included.
     * @param namespace the namespace to assign this library to
     */
    public LibraryLoad(Template template,
            Expression templateName,
            String namespace)
    {
        this.namespace = namespace;
        if (template != null) {
        	String templatePath1 = template.getName();
        	int lastSlash = templatePath1.lastIndexOf('/');
        	templatePath = lastSlash == -1 ? "" : templatePath1.substring(0, lastSlash + 1);
        }
        this.templateName = templateName;
    }
    
    public String getNamespace() {
    	return namespace;
    }
    
    public Expression getTemplateNameExpression() {
    	return templateName;
    }

    public void execute(Environment env) throws TemplateException, IOException {
        String templateNameString = templateName.getStringValue(env);
        if( templateNameString == null ) {
            String msg = "Error " + getStartLocation()
                        + "The expression " + templateName + " is undefined.";
            throw new InvalidReferenceException(msg, env);
        }
        Template importedTemplate;
        try {
            if (templateNameString.indexOf("://") >0) {
                ;
            }
            else if(templateNameString.length() > 0 && templateNameString.charAt(0) == '/')  {
                int protIndex = templatePath.indexOf("://");
                if (protIndex >0) {
                    templateNameString = templatePath.substring(0, protIndex + 2) + templateNameString;
                } else {
                    templateNameString = templateNameString.substring(1);
                }
            }
            else {
                templateNameString = templatePath + templateNameString;
            }
            importedTemplate = env.getTemplateForImporting(templateNameString);
        }
        catch (ParseException pe) {
            String msg = "Error parsing imported template "
                        + templateNameString;
            throw new TemplateException(msg, pe, env);
        }
        catch (IOException ioe) {
            String msg = "Error reading imported file "
                        + templateNameString;
            throw new TemplateException(msg, ioe, env);
        }
        env.importLib(importedTemplate, namespace, false);
    }

    public String getDescription() {
        return "import " + templateName + " as " + namespace;
    }

    public String getTemplateName() {
        return templateName.toString();
    }
}
