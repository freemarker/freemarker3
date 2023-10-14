package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.core.parser.ast.Expression;
import freemarker.core.parser.ast.TemplateElement;

/**
 * An element that represents a conditionally executed block.
 * @author <A HREF="mailto:jon@revusky.com">Jonathan Revusky</A>
 */

public class ConditionalBlock extends TemplateElement {

    private Expression condition;
    private boolean isFirst;

    public ConditionalBlock(Expression condition, TemplateElement nestedBlock, boolean isFirst)
    {
        this.condition = condition;
        this.add(nestedBlock);
        this.isFirst = isFirst;
    }
    
    public Expression getCondition() {
    	return condition;
    }
    
    public void execute(Environment env) throws TemplateException, IOException {
        env.render(firstChildOfType(TemplateElement.class));
    }
    
    public String getDescription() {
        String s = "if ";
        if (condition == null) {
            s = "else ";
        } 
        else if (!isFirst) {
            s = "elseif ";
        }
        String cond = condition != null ? condition.toString() : "";
        return s + cond;
    }
}
