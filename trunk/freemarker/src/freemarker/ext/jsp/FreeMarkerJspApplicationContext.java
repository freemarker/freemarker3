/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
import freemarker.template.utility.ClassUtil;

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
            Class cl = ClassUtil.forName(className);
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