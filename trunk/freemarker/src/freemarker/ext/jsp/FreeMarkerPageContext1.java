package freemarker.ext.jsp;

import freemarker.template.TemplateModelException;

/**
 * Implementation of PageContext that contains JSP 1.1 specific methods. This 
 * class is public to work around Google App Engine Java compliance issues. Do 
 * not use it explicitly.
 * @author Attila Szegedi
 * @version $Id: FreeMarkerPageContext1.java,v 1.2 2005/10/26 17:57:03 revusky Exp $
 */
public class FreeMarkerPageContext1 extends FreeMarkerPageContext {

    public FreeMarkerPageContext1() throws TemplateModelException {
        super();
    }
}
