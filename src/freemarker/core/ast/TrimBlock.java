package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.*;

public class TrimBlock extends TemplateElement {
	
	private boolean left, right;
    
    public TrimBlock(TemplateElement block, boolean left, boolean right) {
    	this.left = left;
    	this.right = right;
        this.nestedBlock = block;
    }
    
    public boolean isLeft() {
    	return left;
    }
    
    public boolean isRight() {
    	return right;
    }

    public void execute(Environment env) throws TemplateException, IOException 
    {
    	if (nestedBlock != null) {
    		env.render(nestedBlock);
    	}
    }

    public String getDescription() {
        return "trim block";
    }
}
