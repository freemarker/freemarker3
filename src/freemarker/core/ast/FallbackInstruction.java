package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

public class FallbackInstruction extends TemplateElement {

    public void execute(Environment env) throws IOException, TemplateException {
        env.fallback();
    }

    public String getDescription() {
        return "fallback instruction";
    }
}
