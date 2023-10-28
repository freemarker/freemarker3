package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.nodes.generated.Expression;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.template.utility.StringUtil;

import static freemarker.core.evaluation.ObjectWrapper.*;

/**
 * Implementations of ?cap_first, ?lower_case, ?upper_case and other
 * built-ins that change a string into another string
 */

public abstract class StringTransformations extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller,
            Object model) {
        String string = null;
        if (isString(model)) {
            string = asString(model);
        }
        else {
            string = caller.getTarget().getStringValue(env);
        }
        if (string == null) {
            throw new InvalidReferenceException("String is undefined", env);
        }
        return apply(string);
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
