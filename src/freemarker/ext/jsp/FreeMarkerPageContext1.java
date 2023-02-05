package freemarker.ext.jsp;

import javax.el.ELContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import freemarker.template.TemplateModelException;

/**
 * Implementation of PageContext that contains JSP 1.1 specific methods. This 
 * class is public to work around Google App Engine Java compliance issues. Do 
 * not use it explicitly.
 * @author Attila Szegedi
 * @version $Id: FreeMarkerPageContext1.java,v 1.2 2005/10/26 17:57:03 revusky Exp $
 */
public class FreeMarkerPageContext1 extends FreeMarkerPageContext {

    public FreeMarkerPageContext1() {
        super();
    }

	@Override
	public ELContext getELContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExpressionEvaluator getExpressionEvaluator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VariableResolver getVariableResolver() {
		// TODO Auto-generated method stub
		return null;
	}
}
