package freemarker.core.evaluation;

import java.io.IOException;
import java.util.Map;

import freemarker.core.Environment;

/**
 * Objects that implement this interface can be used as user-defined directives 
 * (much like macros). They can do arbitrary actions, write arbitrary
 * text to the template output, and trigger rendering of their nested content
 * any number of times.
 *
 * @since 2.3.11
 * @author Attila Szegedi
 * @version $Id: $
 */
public interface UserDirective extends WrappedVariable
{
    /**
     * Executes this user-defined directive; called by FreeMarker when the user-defined
     * directive is called in the template.
     *
     * @param env the current processing environment. Note that you can access
     * the output {@link java.io.Writer Writer} by {@link Environment#getOut()}.
     * @param params the parameters (if any) passed to the directive as a 
     * map of key/value pairs where the keys are {@link String}-s and the 
     * values are {@link WrappedVariable} instances. This is never 
     * <code>null</code>. 
     * @param loopVars an array that corresponds to the "loop variables", in
     * the order as they appear in the directive call. ("Loop variables" are out-parameters
     * that are available to the nested body of the directive; see in the Manual.)
     * You set the loop variables by writing this array. The length of the array gives the
     * number of loop-variables that the caller has specified.
     * Never <code>null</code>, but can be a zero-length array.
     * @param body an object that can be used to render the nested content (body) of
     * the directive call. If the directive call has no nested content (i.e., it is
     * [@myDirective /] or [@myDirective][/@myDirective]), then this will be
     * <code>null</code>.
     *
     * @throws IOException
     */
    public void execute(Environment env, Map<String, Object> params, 
            Object[] loopVars, UserDirectiveBody body) 
    throws IOException;
}