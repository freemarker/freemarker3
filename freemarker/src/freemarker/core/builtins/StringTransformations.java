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
import freemarker.core.ast.Expression;
import freemarker.core.ast.BuiltInExpression;
import freemarker.template.*;
import freemarker.template.utility.StringUtil;

/**
 * Implementations of ?cap_first, ?lower_case, ?upper_case and other
 * built-ins that change a string into another string
 */

public abstract class StringTransformations extends ExpressionEvaluatingBuiltIn {

    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) throws TemplateException {
        String string = null;
        if (model instanceof TemplateScalarModel) {
            string = ((TemplateScalarModel) model).getAsString();
        }
        else {
            string = Expression.getStringValue(model, caller.getTarget(), env);
        }
        if (string == null) {
            throw new InvalidReferenceException("String is undefined", env);
        }
        return new SimpleScalar(apply(string));
    }

    public abstract String apply(String string);
    
    public static class UpperCase extends StringTransformations {
        @Override
        public String apply(String string) {
            return string.toUpperCase();
        }
    }
    
    public static class LowerCase extends StringTransformations {
        @Override
        public String apply(String string) {
            return string.toLowerCase();
        }
    }

    public static class Html extends StringTransformations {
        @Override
        public String apply(String string) {
            return StringUtil.HTMLEnc(string);
        }
    }

    public static class Xhtml extends StringTransformations {
        @Override
        public String apply(String string) {
            return StringUtil.XHTMLEnc(string);
        }
    }

    public static class Xml extends StringTransformations {
        @Override
        public String apply(String string) {
            return StringUtil.XMLEnc(string);
        }
    }

    public static class Rtf extends StringTransformations {
        @Override
        public String apply(String string) {
            return StringUtil.RTFEnc(string);
        }
    }

    public static class CapFirst extends StringTransformations {
        private final boolean cap;
        
        public CapFirst(boolean cap) {
            this.cap = cap;
        }
        
        @Override
        public String apply(String string) {
            for (int i=0; i<string.length(); i++) {
                final char ch = string.charAt(i);
                if (!Character.isWhitespace(ch)) {
                    if ((cap && Character.isUpperCase(ch)) || (!cap && Character.isLowerCase(ch))) {
                        return string;
                    }
                    final char[] chars = string.toCharArray();
                    chars[i] = cap ? Character.toUpperCase(ch) : Character.toLowerCase(ch);
                    return new String(chars);
                }
            }
            return string;
        }
    }

    public static class Trim extends StringTransformations {
        @Override
        public String apply(String string) {
            return string.trim();
        }
    }
    
    public static class Java extends StringTransformations {
        @Override
        public String apply(String string) {
            return StringUtil.javaStringEnc(string);
        }
    }

    public static class JavaScript extends StringTransformations {
        @Override
        public String apply(String string) {
            return StringUtil.javaScriptStringEnc(string);
        }
    }

    public static class Capitalize extends StringTransformations {
        @Override
        public String apply(String string) {
            return StringUtil.capitalize(string);
        }
    }

    public static class Chomp extends StringTransformations {
        @Override
        public String apply(String string) {
            return StringUtil.chomp(string);
        }
    }
}
