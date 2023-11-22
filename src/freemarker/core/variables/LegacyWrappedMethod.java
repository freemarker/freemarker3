
package freemarker.core.variables;

/*
 * 22 October 1999: This class added by Holger Arendt.
 */
import java.util.Arrays;
import java.util.List;

/**
 * Objects that act as methods in a template data model implement this 
 * interface.
 */
public interface LegacyWrappedMethod extends WrappedMethod {

    public Object exec(List<Object> arguments);

    default Object exec(Object... arguments) {
        return exec(Arrays.asList(arguments));
    }
}
