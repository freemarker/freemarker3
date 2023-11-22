package freemarker.template;

import freemarker.core.variables.WrappedVariable;

/**
 * This evolved from FreeMarker 2.x's TemplateScalarModel 
 * and is really not used any more. You can implement this 
 * interface if you want to override * how an object is 
 * interpreted in a string context (the default being to 
 * call Object.toString()). 
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