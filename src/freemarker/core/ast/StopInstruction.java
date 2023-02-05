package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.core.StopException;
import freemarker.template.TemplateException;

/**
 * Represents a &lt;stop&gt; instruction to abort template processing.
 * @author <A HREF="mailto:jon@revusky.com">Jonathan Revusky</A>
 */
public class StopInstruction extends TemplateElement {

    public Expression message;

    public StopInstruction(Expression message) {
        this.message = message;
    }

    public void execute(Environment env) {
        if (message == null) {
            throw new StopException(env);
        }
        throw new StopException(env, message.getStringValue(env));
    }

    public String getDescription() {
        return "stop" + " [" + getStartLocation() + "]";
    }
}


