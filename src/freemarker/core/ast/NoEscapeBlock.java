package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.core.parser.ast.TemplateElement;

/**
 * @version $Id: NoEscapeBlock.java,v 1.1 2003/04/22 21:05:04 revusky Exp $
 * @author Attila Szegedi
 */
public class NoEscapeBlock extends TemplateElement {

    public NoEscapeBlock(TemplateElement nestedBlock) {
        add(nestedBlock);
    }
    
    public void execute(Environment env) throws TemplateException, IOException {
        if (firstChildOfType(TemplateElement.class) != null) {
            env.render(firstChildOfType(TemplateElement.class));
        }
    }

    public String getDescription() {
        return "noescape block";
    }
}
