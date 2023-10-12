package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.core.parser.ast.TemplateElement;
import freemarker.template.utility.StandardCompress;
import java.io.*;

/**
 * An instruction that reduces all sequences of whitespace to a single
 * space or newline. In addition, leading and trailing whitespace is removed.
 * @version $Id: CompressedBlock.java,v 1.1 2003/04/22 21:05:00 revusky Exp $
 * @see freemarker.template.utility.StandardCompress
 */
public class CompressedBlock extends TemplateElement {

    public CompressedBlock(TemplateElement nestedBlock) { 
        this.add(nestedBlock);
    }

    public void execute(Environment env) throws IOException {
        if (firstChildOfType(TemplateElement.class) != null) {
            env.render(firstChildOfType(TemplateElement.class), StandardCompress.INSTANCE, null);
        }
    }

    public String getDescription() {
        return "compressed block";
    }

    public boolean isIgnorable() {
        return firstChildOfType(TemplateElement.class) == null || firstChildOfType(TemplateElement.class).isIgnorable();
    }
}

