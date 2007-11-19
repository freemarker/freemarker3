package freemarker.template;

import java.io.IOException;
import java.io.Writer;

/**
 * Represents the body of a directive invocation. An implementation of this 
 * class is passed to the {@link TemplateDirectiveModel#execute(freemarker.core.Environment, 
 * java.util.Map, TemplateModel[], TemplateDirectiveBody)}. The implementation of the method is
 * free to invoke it any number of times, with any writer.
 *
 * @since 2.3.11
 * @author Attila Szegedi
 * @version $Id: $
 */
public interface TemplateDirectiveBody
{
    /**
     * Renders the body of the directive body to the specified writer.
     * @param out the writer to write the output to.
     */
    public void render(Writer out) throws TemplateException, IOException;
}
