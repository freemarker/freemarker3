package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.*;

public class RecoveryBlock extends TemplateElement {
    
    public RecoveryBlock(TemplateElement block) {
        this.setNestedBlock(block);
    }

    public void execute(Environment env) throws TemplateException, IOException 
    {
    	if (getNestedBlock() != null) {
    		env.render(getNestedBlock());
    	}
    }

    public String getDescription() {
        return "recover block";
    }
}
