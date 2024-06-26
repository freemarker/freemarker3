package freemarker3.template;

import freemarker3.core.variables.WrappedVariable;

/**
 * This evolved from FreeMarker 2.x's TemplateScalarModel 
 * and is really not used any more. You can have your
 * object this implement if you want to override how an object is 
 * displayed in a string (a.k.a. scalar) context (the default being to 
 * call Object.toString()). If you implement getAsString()
 * then that method will be used instead.
 */
public interface TemplateScalarModel extends CharSequence, WrappedVariable {

    default String getAsString() {
        return toString();  
    }

    default char charAt(int i) {
        return getAsString().charAt(i);
    }

    default int length() {
        return getAsString().length();
    }

    default CharSequence subSequence(int start, int end) {
        return getAsString().subSequence(start, end);
    }
}