package freemarker.cache;

import java.io.IOException;
import java.security.CodeSource;

/**
 * A template loader that is able to provide a code source for the template.
 * @author Attila Szegedi
 * @version $Id: $
 */
public interface SecureTemplateLoader extends TemplateLoader
{
    /**
     * Returns a code source for a template source.
     * @param templateSource the template source for which a code source is 
     * requested.
     * @return the code source for the specified template source, or null if it
     * can not be obtained
     * @throws IOException if an I/O exception occurs while trying to obtain 
     * the code source.
     */
    public CodeSource getCodeSource(Object templateSource) throws IOException;
}
