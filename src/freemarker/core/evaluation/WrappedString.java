package freemarker.core.evaluation;

public interface WrappedString extends WrappedVariable, CharSequence {

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