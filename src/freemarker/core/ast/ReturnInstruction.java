package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.core.ReturnException;

/**
 * Represents a &lt;return&gt; instruction to jump out of a macro.
 * @author <A HREF="mailto:jon@revusky.com">Jonathan Revusky</A>
 */
public class ReturnInstruction extends TemplateElement {

    public Expression returnExp;

    public ReturnInstruction(Expression returnExp) {
        this.returnExp = returnExp;
    }

    public void execute(Environment env) {
        if (returnExp != null) {
            env.setLastReturnValue(returnExp.evaluate(env));
        }
        if (nextSibling() != null) {
            // We need to jump out using an exception.
            throw ReturnException.INSTANCE;
        }
        if (!(getParent() instanceof Macro || getParent().getParent() instanceof Macro)) {
            // Here also, we need to jump out using an exception.
            throw ReturnException.INSTANCE;
        }
    }

    public String getDescription() {
        return "return" + " [" + getStartLocation() + "]";
    }
}
