package freemarker.template;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Objects that implement this interface can be used in unified calls much the
 * same as a macro could. They can implement arbitrary code, write arbitrary
 * text to the template output, and trigger rendering of their nested content
 * any number of times. Note that you can also implement macro-like 
 * user-defined directives using {@link TemplateTransformModel} as well. This
 * interface presents a different programming paradigm - for certain purposes
 * it is easier to implement a user-defined directive using this interface, for
 * other purposes you might find {@link TemplateTransformModel} more handy. As
 * far as the template engine is concerned, you can use both equivalently as a 
 * user-defined directive.
 * @author Attila Szegedi
 * @version $Id: $
 */
public interface TemplateRunnableModel extends TemplateModel
{
    /**
     * Executes this runnable once.
     * @param out the writer to write the output to
     * @param args the arguments (if any) passed to the transformation as a 
     * map of key/value pairs where the keys are strings and the arguments are
     * TemplateModel instances. This is never null.
     * @param body an object that can be used to render the body of the 
     * user-defined directive call. If the user-defined directive is 
     * constructed with the empty tag syntax, this will be null.
     * @throws TemplateException
     * @throws IOException
     */
    public void run(Writer out, Map<String, TemplateModel> args, 
            TemplateRunnableBody body) throws TemplateException, IOException;
}