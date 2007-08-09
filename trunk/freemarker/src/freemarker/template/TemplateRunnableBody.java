package freemarker.template;

import java.io.IOException;
import java.io.Writer;

/**
 * Represents the body of a runnable invocation. An implementation of this 
 * class is passed to the {@link TemplateRunnableModel#run(java.io.Writer, 
 * java.util.Map, TemplateRunnableBody)}. The implementation of the method is 
 * free to invoke it any number of times, with any writer.
 * @author Attila Szegedi
 * @version $Id: $
 */
public interface TemplateRunnableBody
{
    /**
     * Renders the body of the template runnable to the specified writer.
     * @param out the writer to write the output to. If null, the environment's
     * writer is used.
     */
    public void render(Writer out) throws TemplateException, IOException;
}
