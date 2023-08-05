package freemarker.core.ast;

import java.util.*;
import java.io.IOException;
import freemarker.template.TemplateException;
import freemarker.core.Environment;


/**
 * Encapsulates an array of <tt>TemplateElement</tt> objects. 
 */
public class MixedContent extends TemplateElement {

    public void addElement(TemplateElement element) {
        add(element);
    }
    
    void prependElement(TemplateElement element) {
        add(0, element);
        element.setParent(this);
    }

    public Iterator<TemplateElement> iteratorTE() {
    	return childrenOfType(TemplateElement.class).iterator();
    }

    /**
     * Processes the contents of the internal <tt>TemplateElement</tt> list,
     * and outputs the resulting text.
     */
    public void execute(Environment env) 
        throws TemplateException, IOException 
    {
        for (TemplateElement element : childrenOfType(TemplateElement.class)) {
            env.render(element);
        }
    }

    public String getDescription() {
        if (getParent() == null) {
            return "root element";
        }
        return "content"; // MixedContent is uninteresting in a stack trace.
    }

    public boolean isIgnorable() {
        return size() == 0;
    }
}
