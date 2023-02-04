package freemarker.core.ast;

import freemarker.core.Environment;

/**
 * An instruction that indicates that that opening
 * and trailing whitespace on this line should be trimmed.
 * @version $Id: TrimInstruction.java,v 1.2 2003/08/29 09:42:38 revusky Exp $
 */
public class TrimInstruction extends TemplateElement {

    private boolean left, right;

    public TrimInstruction(boolean left, boolean right) {
        this.left = left;
        this.right = right;
    }
    
    public boolean isLeft() {
    	return left;
    }
    
    public boolean isRight() {
    	return right;
    }

    public void execute(Environment env) {
        // This instruction does nothing at render-time, only parse-time.
    }

    public String getDescription() {
        String type = "";
        if (!right) type = "left ";
        if (!left) type = "right ";
        if (!left && !right) type = "no-";
        return type + "trim instruction";
    }

    public boolean isIgnorable() {
        return true;
    }
}
