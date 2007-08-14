package freemarker.ext.jsp;

import freemarker.template.TemplateModelException;

import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.PageContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Implementation of PageContext that contains JSP 2.0 specific methods.
 *
 * @author Attila Szegedi
 * @version $Id: FreeMarkerPageContext2.java,v 1.1 2005/06/11 12:13:39 szegedia Exp $
 */
class FreeMarkerPageContext2 extends FreeMarkerPageContext {

    private FreeMarkerPageContext2() throws TemplateModelException {
        super();
    }

    static FreeMarkerPageContext create() throws TemplateModelException {
        return new FreeMarkerPageContext2();
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
}
