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

import freemarker.cache.TemplateCache;
import freemarker.template.*;
import freemarker.template.utility.StringUtil;
import freemarker.template.utility.UndeclaredThrowableException;
import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.parser.ParseException;

/**
 * An instruction that gets another template
 * and processes it within the current template.
 */
public class Include extends TemplateElement {

    private Expression includedTemplateName, encodingExp, parseExp;
    private String encoding;
    private boolean parse;
    private String templatePath="";
    private boolean freshNamespace;

    /**
     * @param template the template that this <tt>Include</tt> is a part of.
     * @param includedTemplateName the name of the template to be included.
     * @param encodingExp the encoding to be used or null, if it is a default.
     * @param parseExp whether the template should be parsed (or is raw text)
     */
    public Include(Template template,
            Expression includedTemplateName,
            boolean freshNamespace,
            Expression encodingExp,
            Expression parseExp) throws ParseException
    {
    	this.freshNamespace = freshNamespace;
    	if (template != null) {
    		String templatePath1 = template.getName();
    		int lastSlash = templatePath1.lastIndexOf('/');
    		templatePath = lastSlash == -1 ? "" : templatePath1.substring(0, lastSlash + 1);
    	}
        this.includedTemplateName = includedTemplateName;
        if (encodingExp instanceof StringLiteral) {
            encoding = encodingExp.toString();
            encoding = encoding.substring(1, encoding.length() -1);
        }
        else {
            this.encodingExp = encodingExp;
        }
        if(parseExp == null) {
            parse = true;
        }
        else if(parseExp.isLiteral()) {
            try {
                if (parseExp instanceof StringLiteral) {
                    parse = StringUtil.getYesNo(parseExp.getStringValue(null));
                }
                else {
                    try {
                        parse = parseExp.isTrue(null);
                    }
                    catch(NonBooleanException e) {
                        throw new ParseException("Expected a boolean or string as the value of the parse attribute", parseExp);
                    }
                }
            }
            catch(TemplateException e) {
                // evaluation of literals must not throw a TemplateException
                throw new UndeclaredThrowableException(e);
            }
        }
        else {
            this.parseExp = parseExp;
        }
    }
    
    public boolean isFreshNamespace() {
    	return freshNamespace;
    }

    public void execute(Environment env) throws TemplateException, IOException {
        String templateNameString = includedTemplateName.getStringValue(env);
        if( templateNameString == null ) {
            String msg = "Error " + getStartLocation()
                        + "The expression " + includedTemplateName + " is undefined.";
            throw new InvalidReferenceException(msg, env);
        }
        String enc = encoding;
        if (encoding == null && encodingExp != null) {
            enc = encodingExp.getStringValue(env);
        }
        
        boolean parse = this.parse;
        if (parseExp != null) {
            TemplateModel tm = parseExp.getAsTemplateModel(env);
            assertNonNull(tm, parseExp, env);
            if (tm instanceof TemplateScalarModel) {
                parse = getYesNo(EvaluationUtil.getString((TemplateScalarModel)tm, parseExp, env));
            }
            else {
                parse = parseExp.isTrue(env);
            }
        }
        
        Template includedTemplate;
        try {
            templateNameString = TemplateCache.getFullTemplatePath(env, templatePath, templateNameString);
            includedTemplate = env.getTemplateForInclusion(templateNameString, enc, parse);
        }
        catch (ParseException pe) {
            String msg = "Error parsing included template "
                        + templateNameString + "\n" + pe.getMessage();
            throw new TemplateException(msg, pe, env);
        }
        catch (IOException ioe) {
            String msg = "Error reading included file "
                        + templateNameString;
            throw new TemplateException(msg, ioe, env);
        }
        env.include(includedTemplate, freshNamespace);
    }
    
    public Expression getIncludedTemplateExpression() {
    	return includedTemplateName;
    }
    
    public Expression getEncodingExp() {
    	return encodingExp;
    }
    
    public Expression getParseExp() {
    	return parseExp;
    }

    public String getDescription() {
    	String name = freshNamespace ? "template " : "include ";
        return name + includedTemplateName;
    }
    
    public boolean useFreshNamespace() {
    	return freshNamespace;
    }

    private boolean getYesNo(String s) throws ParseException {
        try {
           return StringUtil.getYesNo(s);
        }
        catch (IllegalArgumentException iae) {
            throw new ParseException("Error " + getStartLocation()
                 + "\nValue of include parse parameter "
                 + "must be boolean or one of these strings: "
                 + "\"n\", \"no\", \"f\", \"false\", \"y\", \"yes\", \"t\", \"true\""
                 + "\nFound: " + parseExp, parseExp);
        }
    }
}
