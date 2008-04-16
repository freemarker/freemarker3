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

import java.io.StringReader;

import sun.rmi.runtime.GetThreadPoolAction;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.Expression;
import freemarker.core.parser.FMParser;
import freemarker.core.parser.FMParserConstants;
import freemarker.core.parser.FMParserTokenManager;
import freemarker.core.parser.ParseException;
import freemarker.core.parser.SimpleCharStream;
import freemarker.template.*;

/**
 * Implementation of ?eval built-in 
 */

public class evalBI extends BuiltIn {
	
	public TemplateModel get(TemplateModel target, String builtInName, Environment env, BuiltInExpression callingExpression) throws TemplateException {
		try {
			String evalString = ((TemplateScalarModel) target).getAsString();
			return eval(evalString, env, callingExpression);
		} catch (ClassCastException cce) {
			throw new TemplateModelException("Expecting string on left of ?eval built-in");
			
		} catch (NullPointerException npe) {
			throw new TemplateModelException(npe);
		}
	}
	
    TemplateModel eval(String s, Environment env, BuiltInExpression callingExpression) throws TemplateException 
    {
        SimpleCharStream scs = new SimpleCharStream(
                new StringReader("(" + s + ")"), callingExpression.getBeginLine(),
                callingExpression.getBeginColumn(), s.length() + 2);
        FMParserTokenManager token_source = new FMParserTokenManager(scs);
        token_source.SwitchTo(FMParserConstants.EXPRESSION);
        FMParser parser = new FMParser(token_source);
        parser.setTemplate(callingExpression.getTemplate());
        Expression exp = null;
        try {
            exp = parser.Exp();
        } catch (ParseException pe) {
            pe.setTemplateName(callingExpression.getTemplate().getName());
            throw new TemplateException(pe, env);
        }
        return exp.getAsTemplateModel(env);
    }
}