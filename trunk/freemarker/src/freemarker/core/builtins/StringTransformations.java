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

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.template.*;
import freemarker.template.utility.StringUtil;

/**
 * Implementations of ?cap_first, ?lower_case, ?upper_case and other
 * built-ins that change a string into another string
 */

public class StringTransformations extends BuiltIn {
	
	public TemplateModel get(TemplateModel target, String builtInName, Environment env, BuiltInExpression callingExpression) throws TemplateException {
		try {
			String string = ((TemplateScalarModel) target).getAsString();
			return new SimpleScalar(convertString(string, builtInName));
		} catch (ClassCastException cce) {
			throw callingExpression.invalidTypeException(target, callingExpression.getTarget(), env, "string");
		} catch (NullPointerException npe) {
			throw new InvalidReferenceException("String is undefined", env);
		}
	}
	
	private String convertString(String string, String builtInName) {
		if (builtInName == "upper_case") {
			return string.toUpperCase();
		} 
		if (builtInName == "lower_case") {
			return string.toLowerCase();
		}
		if (builtInName == "html" || builtInName == "web_safe") {
			return StringUtil.HTMLEnc(string);
		}
		if (builtInName == "xml") {
			return StringUtil.XMLEnc(string);
		}
		if (builtInName == "xhtml") {
			return StringUtil.XHTMLEnc(string);
		}
		if (builtInName == "rtf") {
			return StringUtil.RTFEnc(string);
		}
		if (builtInName == "cap_first" || builtInName == "uncap_first") {
			char[] chars = string.toCharArray();
			for (int i=0; i<chars.length; i++) {
				char ch = chars[i];
				if (!Character.isWhitespace(ch)) {
					if (builtInName == "cap_first") {
						chars[i] = Character.toUpperCase(ch);
					} else {
						chars[i] = Character.toLowerCase(ch);
					}
					break;
				}
			}
			return new String(chars);
		}
		if (builtInName == "trim") {
			return string.trim();
		}
		if (builtInName == "j_string") {
			return StringUtil.javaStringEnc(string);
		}
		if (builtInName == "js_string") {
			return StringUtil.javaScriptStringEnc(string);
		}
		if (builtInName == "capitalize") {
			return StringUtil.capitalize(string);
		}
		if (builtInName == "chop_linebreak") {
			return StringUtil.chomp(string);
		}
		throw new InternalError("Cannot deal with built-in ?" + builtInName);
	}
}
