
 package freemarker.ext.dom;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public interface XPathSupport {
    TemplateModel executeQuery(Object context, String xpathQuery) throws TemplateModelException;
}
