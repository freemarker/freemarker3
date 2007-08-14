package freemarker.ext.jsp;

import freemarker.template.TemplateModelException;

/**
 * @author Attila Szegedi
 * @version $Id: FreeMarkerPageContext1.java,v 1.2 2005/10/26 17:57:03 revusky Exp $
 */
class FreeMarkerPageContext1 extends FreeMarkerPageContext {

    private FreeMarkerPageContext1() throws TemplateModelException {
        super();
    }

    static FreeMarkerPageContext create() throws TemplateModelException {
        return new FreeMarkerPageContext1();
    }
}
