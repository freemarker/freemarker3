package freemarker.ext.util;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;

/**
 * Interface used to create various wrapper models in the {@link ModelCache}.
 * @version $Id: ModelFactory.java,v 1.6 2003/01/12 23:40:16 revusky Exp $
 * @author Attila Szegedi, szegedia at freemail dot hu
 */
public interface ModelFactory
{
    /**
     * Create a wrapping model for the specified object that belongs to
     * the specified wrapper.
     */
    TemplateModel create(Object object, BeansWrapper wrapper);
}
