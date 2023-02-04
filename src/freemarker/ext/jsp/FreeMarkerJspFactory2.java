package freemarker.ext.jsp;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class FreeMarkerJspFactory2 extends FreeMarkerJspFactory
{
    @Override
    protected String getSpecificationVersion() {
        return "2.0";
    }

	@Override
	public JspApplicationContext getJspApplicationContext(ServletContext arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}