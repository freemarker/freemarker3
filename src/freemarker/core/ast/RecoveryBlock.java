package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.core.parser.ast.TemplateElement;

public class RecoveryBlock extends TemplateElement {
    
    public RecoveryBlock(TemplateElement block) {
        this.add(block);
    }

    public void execute(Environment env) throws TemplateException, IOException 
    {
    	if (firstChildOfType(TemplateElement.class) != null) {
    		env.render(firstChildOfType(TemplateElement.class));
    	}
    }

    public String getDescription() {
        return "recover block";
    }
}
