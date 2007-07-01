/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
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

    public final Expression templateName;
    public final String namespace;
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

    public void execute(Environment env) throws TemplateException, IOException {
        String templateNameString = templateName.getStringValue(env);
        if( templateNameString == null ) {
            String msg = "Error " + getStartLocation()
                        + "The expression " + templateName + " is undefined.";
            throw new InvalidReferenceException(msg, env);
        }
        Template importedTemplate;
        try {
            if(!env.isClassicCompatible()) {
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
        env.importLib(importedTemplate, namespace);
    }

    public String getDescription() {
        return "import " + templateName + " as " + namespace;
    }

    public String getTemplateName() {
        return templateName.toString();
    }
}
