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
        nestedElements = new ArrayList<TemplateElement>();
        addBlock(block);
    }

    public void addBlock(ConditionalBlock block) {
        nestedElements.add(block);
    }
    
    public List<TemplateElement> getSubBlocks() {
    	return Collections.unmodifiableList(nestedElements);
    }
    
    public void execute(Environment env) throws TemplateException, IOException {
    	
        for (TemplateNode te : nestedElements) {
            ConditionalBlock cblock = (ConditionalBlock) te;
            Expression condition = cblock.getCondition();
            if (condition == null || condition.isTrue(env)) {
                if (cblock.getNestedBlock() != null) {
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
