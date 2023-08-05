package freemarker.core.ast;

import java.util.*;
import java.io.IOException;
import freemarker.template.TemplateException;
import freemarker.core.Environment;


/**
 * Encapsulates an array of <tt>TemplateElement</tt> objects. 
 */
public class MixedContent extends TemplateElement {

    public MixedContent()
    {
        nestedElements = new ArrayList<TemplateElement>();
    }

    public void addElement(TemplateElement element) {
        nestedElements.add(element);
    }
    
    void prependElement(TemplateElement element) {
        element.setParent(this);
        List<TemplateElement> newList = new ArrayList<TemplateElement>();
        newList.add(element);
        for (TemplateElement te : nestedElements) {
            newList.add(te);
        }
        this.nestedElements = newList;
    }

    public Iterator<TemplateElement> iteratorTE() {
    	return nestedElements.iterator();
    }

    /**
     * Processes the contents of the internal <tt>TemplateElement</tt> list,
     * and outputs the resulting text.
     */
    public void execute(Environment env) 
        throws TemplateException, IOException 
    {
        for (int i=0; i<nestedElements.size(); i++) {
            TemplateElement element = nestedElements.get(i);
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
        return nestedElements == null || nestedElements.size() == 0;
    }
}
