package freemarker.core.ast;

import java.util.*;
import java.io.IOException;
import freemarker.template.TemplateException;
import freemarker.core.Environment;
import freemarker.core.parser.ast.Expression;
import freemarker.core.parser.ast.TemplateElement;
import freemarker.core.parser.ast.TemplateNode;

/**
 * A instruction that handles if-elseif-else blocks.
 * @author <A HREF="mailto:jon@revusky.com">Jonathan Revusky</A>
 */

public class IfBlock extends TemplateElement {

    public IfBlock(ConditionalBlock block)
    {
        addBlock(block);
    }

    public void addBlock(ConditionalBlock block) {
        add(block);
    }
    
    public void execute(Environment env) throws TemplateException, IOException {
    	
        for (ConditionalBlock cblock : childrenOfType(ConditionalBlock.class)) {
            Expression condition = cblock.getCondition();
            if (condition == null || condition.isTrue(env)) {
                if (cblock.firstChildOfType(TemplateElement.class) != null) {
                    env.render(cblock);
                }
                return;
            }
        }
    }

    public String getDescription() {
        return "if-else ";
    }
}
