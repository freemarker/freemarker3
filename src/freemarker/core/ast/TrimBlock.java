package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.core.parser.ast.TemplateElement;

public class TrimBlock extends TemplateElement {
	
	private boolean left, right;
    
    public TrimBlock(TemplateElement block, boolean left, boolean right) {
    	this.left = left;
    	this.right = right;
        this.add(block);
    }
    
    public boolean isLeft() {
    	return left;
    }
    
    public boolean isRight() {
    	return right;
    }

    public void execute(Environment env) throws IOException 
    {
    	if (firstChildOfType(TemplateElement.class) != null) {
    		env.render(firstChildOfType(TemplateElement.class));
    	}
    }

    public String getDescription() {
        return "trim block";
    }
}
