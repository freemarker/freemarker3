package freemarker3.core.variables;

import java.io.IOException;
import java.io.Writer;

/**
 * Represents the body of a directive invocation. An implementation of this 
 * class is passed to the {@link UserDirective#execute(freemarker3.core.Environment, 
 * java.util.Map, WrappedVariable[], UserDirectiveBody)}. The implementation of the method is
 * free to invoke it any number of times, with any writer.
 *
 * @since 2.3.11
 * @author Attila Szegedi
 * @version $Id: $
 */
public interface UserDirectiveBody
{
    /**
     * Renders the body of the directive body to the specified writer. The 
     * writer is not flushed after the rendering. If you pass the environment's
     * writer, there is no need to flush it. If you supply your own writer, you
     * are responsible to flush/close it when you're done with using it (which
     * might be after multiple renderings).
     * @param out the writer to write the output to.
     */
    public void render(Writer out) throws IOException;
}
