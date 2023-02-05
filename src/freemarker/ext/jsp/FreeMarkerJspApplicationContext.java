package freemarker.ext.jsp;

import java.util.LinkedList;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.el.ImplicitObjectELResolver;
import javax.servlet.jsp.el.ScopedAttributeELResolver;

import freemarker.log.Logger;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class FreeMarkerJspApplicationContext implements JspApplicationContext
{
    private static final Logger logger = Logger.getLogger("freemarker.jsp");
    private static final ExpressionFactory expressionFactoryImpl = findExpressionFactoryImplementation();
    
    private final LinkedList<ELContextListener> listeners = new LinkedList<ELContextListener>();
    private final CompositeELResolver elResolver = new CompositeELResolver();
    private final CompositeELResolver additionalResolvers = new CompositeELResolver();
    {
        elResolver.add(new ImplicitObjectELResolver());
        elResolver.add(additionalResolvers);
        elResolver.add(new MapELResolver());
        elResolver.add(new ResourceBundleELResolver());
        elResolver.add(new ListELResolver());
        elResolver.add(new ArrayELResolver());
        elResolver.add(new BeanELResolver());
        elResolver.add(new ScopedAttributeELResolver());
    }
    
    public void addELContextListener(ELContextListener listener) {
        synchronized(listeners) {
            listeners.addLast(listener);
        }
    }

    private static ExpressionFactory findExpressionFactoryImplementation() {
        ExpressionFactory ef = tryExpressionFactoryImplementation("com.sun");
        if(ef == null) {
            ef = tryExpressionFactoryImplementation("org.apache");
            if(ef == null) {
                logger.warn("Could not find any implementation for " + 
                        ExpressionFactory.class.getName());
            }
        }
        return ef;
    }

    private static ExpressionFactory tryExpressionFactoryImplementation(String packagePrefix) {
        String className = packagePrefix + ".el.ExpressionFactoryImpl";
        try {
            Class cl = Class.forName(className);
            if(ExpressionFactory.class.isAssignableFrom(cl)) {
                logger.info("Using " + className + " as implementation of " + 
                        ExpressionFactory.class.getName());
                return (ExpressionFactory)cl.newInstance();
            }
            logger.warn("Class " + className + " does not implement " + 
                    ExpressionFactory.class.getName());
        }
        catch(ClassNotFoundException e) {
        }
        catch(Exception e) {
            logger.error("Failed to instantiate " + className, e);
        }
        return null;
    }

    public void addELResolver(ELResolver resolver) {
        additionalResolvers.add(resolver);
    }

    public ExpressionFactory getExpressionFactory() {
        return expressionFactoryImpl;
    }
    
    ELContext createNewELContext(final FreeMarkerPageContext pageCtx) {
        ELContext ctx = new FreeMarkerELContext(pageCtx);
        ELContextEvent event = new ELContextEvent(ctx);
        synchronized(listeners) {
            for (ELContextListener l : listeners) {
                l.contextCreated(event);
            }
        }
        return ctx;
    }

    private class FreeMarkerELContext extends ELContext {
        private final FreeMarkerPageContext pageCtx;
        
        FreeMarkerELContext(FreeMarkerPageContext pageCtx) {
            this.pageCtx = pageCtx;
        }
        
        @Override
        public ELResolver getELResolver() {
            return elResolver;
        }

        @Override
        public FunctionMapper getFunctionMapper() {
            return null;
        }

        @Override
        public VariableMapper getVariableMapper() {
            return new VariableMapper() {
                @Override
                public ValueExpression resolveVariable(String name) {
                    Object obj = pageCtx.findAttribute(name);
                    if(obj == null) {
                        return null;
                    }
                    return expressionFactoryImpl.createValueExpression(obj, 
                            obj.getClass());
                }

                @Override
                public ValueExpression setVariable(String name, 
                        ValueExpression value) {
                    ValueExpression prev = resolveVariable(name);
                    pageCtx.setAttribute(name, value.getValue(
                            FreeMarkerELContext.this));
                    return prev;
                }
            };
        }
    }
}