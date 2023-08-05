package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

/**
 * @version $Id: NoEscapeBlock.java,v 1.1 2003/04/22 21:05:04 revusky Exp $
 * @author Attila Szegedi
 */
public class NoEscapeBlock extends TemplateElement {

    public NoEscapeBlock(TemplateElement nestedBlock) {
        setNestedBlock(nestedBlock);
    }
    
    public void execute(Environment env) throws TemplateException, IOException {
        if (getNestedBlock() != null) {
            env.render(getNestedBlock());
        }
    }

    public String getDescription() {
        return "noescape block";
    }
}
