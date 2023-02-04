package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.*;

public class RecoveryBlock extends TemplateElement {
    
    public RecoveryBlock(TemplateElement block) {
        this.nestedBlock = block;
    }

    public void execute(Environment env) throws TemplateException, IOException 
    {
    	if (nestedBlock != null) {
    		env.render(nestedBlock);
    	}
    }

    public String getDescription() {
        return "recover block";
    }
}
