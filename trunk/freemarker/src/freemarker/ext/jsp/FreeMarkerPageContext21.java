package freemarker.ext.jsp;

import freemarker.log.Logger;
import freemarker.template.TemplateModelException;

import javax.el.ELContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Implementation of PageContext that contains JSP 2.0 and JSP 2.1 specific methods.
 *
 * @author Attila Szegedi
 * @version $Id: FreeMarkerPageContext2.java,v 1.1 2005/06/11 12:13:39 szegedia Exp $
 */
class FreeMarkerPageContext21 extends FreeMarkerPageContext {
    private static final Logger logger = Logger.getLogger("freemarker.jsp");

    static {
        if(JspFactory.getDefaultFactory() == null) {
            JspFactory.setDefaultFactory(new FreeMarkerJspFactory21());
        }
        logger.debug("Using JspFactory implementation class " + 
                JspFactory.getDefaultFactory().getClass().getName());
    }

    private FreeMarkerPageContext21() throws TemplateModelException {
        super();
    }

    static FreeMarkerPageContext create() throws TemplateModelException {
        return new FreeMarkerPageContext21();
    }

    /**
     * Attempts to locate and manufacture an expression evaulator instance. For this
     * to work you <b>must</b> have the Apache Commons-EL package in the classpath. If
     * Commons-EL is not available, this method will throw an UnsupportedOperationException. 
     */
    public ExpressionEvaluator getExpressionEvaluator() {
        try {
            Class type = AccessController.doPrivileged(
                    new PrivilegedAction<ClassLoader>() {
                        public ClassLoader run() {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    }).loadClass
                    ("org.apache.commons.el.ExpressionEvaluatorImpl");
            return (ExpressionEvaluator) type.newInstance();
        }
        catch (Exception e) {
            throw new UnsupportedOperationException("In order for the getExpressionEvaluator() " +
                "method to work, you must have downloaded the apache commons-el jar and " +
                "made it available in the classpath.");
        }
    }

    /**
     * Returns a variable resolver that will resolve variables by searching through
     * the page scope, request scope, session scope and application scope for an
     * attribute with a matching name.
     */
    public VariableResolver getVariableResolver() {
        final PageContext ctx = this;

        return new VariableResolver() {
            public Object resolveVariable(String name) throws ELException {
                return ctx.findAttribute(name);
            }
        };
    }

    private ELContext elContext;
    
    @Override
    public ELContext getELContext() {
        if(elContext == null) { 
            JspApplicationContext jspctx = JspFactory.getDefaultFactory().getJspApplicationContext(getServletContext());
            if(jspctx instanceof FreeMarkerJspApplicationContext) {
                elContext = ((FreeMarkerJspApplicationContext)jspctx).createNewELContext(this);
                elContext.putContext(JspContext.class, this);
            }
            else {
                throw new UnsupportedOperationException(
                        "Can not create an ELContext using a foreign JspApplicationContext\n" +
                        "Consider dropping a private instance of JSP 2.1 API JAR file in\n" +
                        "your WEB-INF/lib directory and then try again.");
            }
        }
        return elContext;
    }
}
