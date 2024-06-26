package freemarker3.core;

import java.io.IOException;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public  interface TemplateRunnable<T> {
    public T run() throws IOException;
}
