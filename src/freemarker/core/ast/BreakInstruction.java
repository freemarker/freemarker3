package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.core.BreakException;

/**
 * Represents a &lt;break&gt; instruction to break out of a loop.
 * @author <A HREF="mailto:jon@revusky.com">Jonathan Revusky</A>
 */
public class BreakInstruction extends TemplateElement {

    public void execute(Environment env) {
        throw BreakException.INSTANCE;
    }

    public String getDescription() {
        return "break" + " [" + getStartLocation() + "]";
    }
}


