package freemarker.core;

import java.io.IOException;

import freemarker.core.ast.TemplateElement;
import freemarker.template.TemplateException;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class SecureRendererImpl extends SecureRenderer
{
    @Override
    public void render(Environment e, TemplateElement t) throws TemplateException, IOException
    {
        e.render(t);
    }
}
