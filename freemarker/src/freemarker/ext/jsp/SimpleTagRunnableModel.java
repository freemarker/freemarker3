package freemarker.ext.jsp;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.Tag;

import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateRunnableBody;
import freemarker.template.TemplateRunnableModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class SimpleTagRunnableModel extends JspTagModelBase<SimpleTag> implements TemplateRunnableModel
{
    protected SimpleTagRunnableModel(Class<? extends SimpleTag> tagClass) throws IntrospectionException {
        super(tagClass);
        if(!SimpleTag.class.isAssignableFrom(tagClass)) {
            throw new IllegalArgumentException(tagClass.getName() + 
                    " does not implement either the " + Tag.class.getName() + 
                    " interface or the " + SimpleTag.class.getName() + 
                    " interface.");
        }
    }

    public void run(Writer out, Map<String, TemplateModel> args, final TemplateRunnableBody body) 
    throws TemplateException, IOException {
        try {
            SimpleTag tag = getTagInstance();
            final FreeMarkerPageContext pageContext = PageContextFactory.getCurrentPageContext();
            tag.setJspContext(pageContext);
            Tag parentTag = pageContext.peekTopTag();
            if(parentTag != null) {
                tag.setParent(parentTag);
            }
            setupTag(tag, args, pageContext.getObjectWrapper());
            if(body != null) {
                tag.setJspBody(new JspFragment() {
                    @Override
                    public JspContext getJspContext() {
                        return pageContext;
                    }
                    
                    @Override
                    public void invoke(Writer out) throws JspException, IOException {
                        try {
                            body.render(out);
                        }
                        catch(TemplateException e) {
                            throw new JspException(e);
                        }
                    }
                });
            }
            //TODO: set it as top tag?
            tag.doTag();
        }
        catch(TemplateException e) {
            throw e;
        }
        catch(RuntimeException e) {
            throw e;
        }
        catch(Exception e) {
            throw new TemplateModelException(e);
        }
    }
}
