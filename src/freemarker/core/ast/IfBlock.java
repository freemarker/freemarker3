package freemarker.core.ast;

import java.util.*;
import java.io.IOException;
import freemarker.template.TemplateException;
import freemarker.core.Environment;
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
    
    public List<TemplateElement> getSubBlocks() {
    	return Collections.unmodifiableList(childrenOfType(TemplateElement.class));
    }
    
    public void execute(Environment env) throws TemplateException, IOException {
    	
        for (TemplateNode te : childrenOfType(TemplateElement.class)) {
            ConditionalBlock cblock = (ConditionalBlock) te;
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
