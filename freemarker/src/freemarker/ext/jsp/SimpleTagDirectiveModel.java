package freemarker.ext.jsp;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.Tag;

import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class SimpleTagDirectiveModel extends JspTagModelBase<SimpleTag> implements TemplateDirectiveModel
{
    protected SimpleTagDirectiveModel(Class<? extends SimpleTag> tagClass) throws IntrospectionException {
        super(tagClass);
        if(!SimpleTag.class.isAssignableFrom(tagClass)) {
            throw new IllegalArgumentException(tagClass.getName() + 
                    " does not implement either the " + Tag.class.getName() + 
                    " interface or the " + SimpleTag.class.getName() + 
                    " interface.");
        }
    }

    public void execute(Writer out, Map<String, TemplateModel> args, final TemplateDirectiveBody body) 
    throws TemplateException, IOException {
        try {
            SimpleTag tag = getTagInstance();
            final FreeMarkerPageContext pageContext = PageContextFactory.getCurrentPageContext();
            tag.setJspContext(pageContext);
            JspTag parentTag = pageContext.peekTopTag(JspTag.class);
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
                pageContext.pushTopTag(tag);
                try {
                    tag.doTag();
                }
                finally {
                    pageContext.popTopTag();
                }
            }
            else {
                tag.doTag();
            }
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
