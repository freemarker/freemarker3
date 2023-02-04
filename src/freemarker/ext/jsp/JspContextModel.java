package freemarker.ext.jsp;

import javax.servlet.jsp.PageContext;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

class JspContextModel
implements
    TemplateHashModel
{
    public static final int ANY_SCOPE = -1;
    public static final int PAGE_SCOPE = PageContext.PAGE_SCOPE;
    public static final int REQUEST_SCOPE = PageContext.REQUEST_SCOPE;
    public static final int SESSION_SCOPE = PageContext.SESSION_SCOPE;
    public static final int APPLICATION_SCOPE = PageContext.APPLICATION_SCOPE;

    private final PageContext pageContext;
    private final int scope;

    public JspContextModel(PageContext pageContext, int scope)
    {
        this.pageContext = pageContext;
        this.scope = scope;
    }

    public TemplateModel get(String key) throws TemplateModelException
    {
        Object bean = scope == ANY_SCOPE ? pageContext.findAttribute(key) : pageContext.getAttribute(key, scope);
        return BeansWrapper.getDefaultInstance().wrap(bean);
    }

    public boolean isEmpty()
    {
        return false;
    }
}
