package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;

/**
 * An instruction that flushes the output stream.
 * @version $Id: FlushInstruction.java,v 1.2 2004/01/06 17:06:42 szegedia Exp $
 */
public class FlushInstruction extends TemplateElement {

    public void execute(Environment env) throws IOException {
        env.getOut().flush();
    }

    public String getDescription() {
        return "flush instruction";
    }
}
