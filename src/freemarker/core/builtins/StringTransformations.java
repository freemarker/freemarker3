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
