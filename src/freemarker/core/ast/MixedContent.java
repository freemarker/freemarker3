package freemarker.core.ast;

import java.io.IOException;
import freemarker.core.Environment;
import freemarker.core.parser.ast.TemplateElement;

/**
 * Encapsulates an array of <tt>TemplateElement</tt> objects. 
 */
public class MixedContent extends TemplateElement {

    /**
     * Processes the contents of the internal <tt>TemplateElement</tt> list,
     * and outputs the resulting text.
     */
    public void execute(Environment env) throws IOException 
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
}
