package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.*;
import freemarker.template.*;

/**
 * An instruction that processes the nested block within a macro instruction.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
public class BodyInstruction extends TemplateElement {
    
    
//    private List bodyParameters;
	private PositionalArgsList args;
    
    public BodyInstruction(PositionalArgsList args) {
    	this.args = args;
    }
    
    public ArgsList getArgs() {
    	return args;
    }
    

    /**
     * There is actually a subtle but essential point in the code below.
     * A macro operates in the context in which it is defined. However, 
     * a nested block within a macro instruction is defined in the 
     * context in which the macro was invoked. So, we actually need to
     * temporarily switch the namespace and macro context back to
     * what it was before macro invocation to implement this properly.
     * I (JR) realized this thanks to some incisive comments from Daniel Dekany.
     */
    public void execute(Environment env) throws IOException, TemplateException {
        MacroInvocationBodyContext bodyContext = new MacroInvocationBodyContext(env, args);
        env.render(bodyContext);
    }

    public String getDescription() {
        return "nested macro content";
    }
}
