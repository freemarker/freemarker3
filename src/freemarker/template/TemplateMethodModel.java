
package freemarker.template;

/*
 * 22 October 1999: This class added by Holger Arendt.
 * Actually, I think it took a list of strings originally
 * and then there was TemplateMethodModelEx (now gone)
 * that took a list of objects. The interface has been
 * retrofitted into the newer freemarker.core.variables.Callable
 * interface. It is provided to make it somewhat easier
 * to migrate older code. But if you are not migrating older
 * code there is no real reason to use this interface.
 */
import java.util.Arrays;
import java.util.List;

import freemarker.core.variables.VarArgsFunction;

public interface TemplateMethodModel extends VarArgsFunction<Object> {

    public Object exec(List<Object> arguments);

    default Object apply(Object... arguments) {
        return exec(Arrays.asList(arguments));
    }
}
