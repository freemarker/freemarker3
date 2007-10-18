package freemarker.template;

import java.io.IOException;
import java.util.Map;

import freemarker.core.Environment;

/**
 * Objects that implement this interface can be used as user-defined directives 
 * (much like macros). They can implement arbitrary code, write arbitrary
 * text to the template output, and trigger rendering of their nested content
 * any number of times.
 * @author Attila Szegedi
 * @version $Id: $
 */
public interface TemplateDirectiveModel extends TemplateModel
{
    /**
     * Executes this directive once.
     * @param env the current processing environment
     * @param args the arguments (if any) passed to the transformation as a 
     * map of key/value pairs where the keys are strings and the arguments are
     * TemplateModel instances. This is never null.
     * @param body an object that can be used to render the body of the 
     * user-defined directive call. If the user-defined directive is 
     * constructed with the empty tag syntax, this will be null.
     * @throws TemplateException
     * @throws IOException
     */
    public void execute(Environment env, Map<String, TemplateModel> args, 
            TemplateDirectiveBody body) throws TemplateException, IOException;
}