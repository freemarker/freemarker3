package freemarker.ext.jsp;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class FreeMarkerJspFactory21 extends FreeMarkerJspFactory
{
    private static final String JSPCTX_KEY = 
        FreeMarkerJspApplicationContext.class.getName();

    @Override
    protected String getSpecificationVersion() {
        return "2.1";
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
}