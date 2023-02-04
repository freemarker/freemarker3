package freemarker.core;

import java.io.IOException;

import freemarker.template.TemplateException;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public  interface TemplateRunnable<T> {
    public T run() throws TemplateException, IOException;
}
