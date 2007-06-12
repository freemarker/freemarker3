package freemarker.ext.jsp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.jsp.PageContext;

import freemarker.core.Environment;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.UndeclaredThrowableException;

/**
 * @author Attila Szegedi
 * @version $Id: PageContextFactory.java,v 1.2 2005/06/11 21:21:09 szegedia Exp $
 */
class PageContextFactory {

    private static final Method constructor;
    
    static {
        Class impl;
        try {
            try {
                PageContext.class.getMethod("getExpressionEvaluator", (Class[]) null);
                impl = Class.forName("freemarker.ext.jsp.FreeMarkerPageContext2");
            }
            catch(NoSuchMethodException e) {
                impl = Class.forName("freemarker.ext.jsp.FreeMarkerPageContext1");
            }
            constructor = impl.getDeclaredMethod("create", (Class[]) null);
        }
        catch(ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
        catch(NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    static FreeMarkerPageContext getCurrentPageContext() throws TemplateModelException {
        Environment env = Environment.getCurrentEnvironment();
        TemplateModel pageContextModel = env.get(PageContext.PAGECONTEXT);
        if(pageContextModel instanceof FreeMarkerPageContext) {
            return (FreeMarkerPageContext)pageContextModel;
        }
        try {
            FreeMarkerPageContext pageContext = 
                (FreeMarkerPageContext)constructor.invoke(null, (Object[]) null);
            env.setGlobalVariable(PageContext.PAGECONTEXT, pageContext);
            return pageContext;
        }
        catch(IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
        catch(InvocationTargetException e) {
            if(e.getTargetException() instanceof TemplateModelException) {
                throw (TemplateModelException)e.getTargetException();
            }
            throw new UndeclaredThrowableException(e);
        }
    }
    
}
