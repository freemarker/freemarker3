package freemarker.ext.jsp;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspEngineInfo;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class FreeMarkerJspFactory extends JspFactory
{
    private static final String JSPCTX_KEY = 
        FreeMarkerJspApplicationContext.class.getName();

    @Override
    public JspEngineInfo getEngineInfo() {
        return new JspEngineInfo() {
            @Override
            public String getSpecificationVersion() {
                return "2.1";
            }
        };
    }

    @Override
    public JspApplicationContext getJspApplicationContext(ServletContext ctx) {
        JspApplicationContext jspctx = (JspApplicationContext)ctx.getAttribute(
                JSPCTX_KEY);
        if(jspctx == null) {
            synchronized(ctx) {
                jspctx = (JspApplicationContext)ctx.getAttribute(JSPCTX_KEY);
                if(jspctx == null) {
                    jspctx = new FreeMarkerJspApplicationContext();
                    ctx.setAttribute(JSPCTX_KEY, jspctx);
                }
            }
        }
        return jspctx;
    }

    @Override
    public PageContext getPageContext(Servlet servlet, ServletRequest request, 
            ServletResponse response, String errorPageURL, 
            boolean needsSession, int bufferSize, boolean autoFlush) {
        // This is never meant to be called. JSP pages compiled to Java 
        // bytecode use this API, but in FreeMarker, we're running templates,
        // and not JSP pages precompiled to bytecode, therefore we have no use
        // for this API.
        throw new UnsupportedOperationException();
    }

    @Override
    public void releasePageContext(PageContext ctx) {
        // This is never meant to be called. JSP pages compiled to Java 
        // bytecode use this API, but in FreeMarker, we're running templates,
        // and not JSP pages precompiled to bytecode, therefore we have no use
        // for this API.
        throw new UnsupportedOperationException();
    }
}