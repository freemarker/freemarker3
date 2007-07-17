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

import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.core.parser.*;
import java.io.*;

public class StringLiteral extends Expression implements TemplateScalarModel {
    
    public final String value;
    public final boolean raw;
    private TemplateElement interpolatedOutput;
    
    public StringLiteral(String value, boolean raw) {
        this.value = value;
        this.raw = raw;
    }
    
    public void checkInterpolation() throws ParseException {
        if (value.length() >3 && value.indexOf("${") >= 0 || value.indexOf("#{") >= 0) {
            SimpleCharStream scs = new SimpleCharStream(new StringReader(value), getBeginLine(), getBeginColumn()+1, value.length());
            FMParserTokenManager token_source = new FMParserTokenManager(scs);
            token_source.setOnlyTextOutput(true);
            FMParser parser = new FMParser(token_source);
            parser.setTemplate(getTemplate());
            interpolatedOutput = parser.FreeMarkerText();
            this.constantValue = null;
        }
    }
    
    TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
        return new SimpleScalar(getStringValue(env));
    }

    public String getAsString() {
        return value;
    }
    
    String getStringValue(Environment env) throws TemplateException {
        if (interpolatedOutput == null) {
            return value;
        } 
        else {
            TemplateExceptionHandler teh = env.getTemplateExceptionHandler();
            env.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            try {
               return env.renderElementToString(interpolatedOutput);
            }
            catch (IOException ioe) {
                throw new TemplateException(ioe, env);
            }
            finally {
                env.setTemplateExceptionHandler(teh);
            }
        }
    }

    boolean _isLiteral() {
        return interpolatedOutput == null;
    }

    Expression _deepClone(String name, Expression subst) {
        StringLiteral cloned = new StringLiteral(value, raw);
        cloned.interpolatedOutput = this.interpolatedOutput;
        return cloned;
    }

    static public String escapeString(String s) {
        if (s.indexOf('"') == -1) {
            return s;
        }
        java.util.StringTokenizer st = new java.util.StringTokenizer(s, "\"", true);
        StringBuffer buf = new StringBuffer();
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("\"")) {
                buf.append('\\');
            }
            buf.append(tok);
        }
        return buf.toString();
    }
}
