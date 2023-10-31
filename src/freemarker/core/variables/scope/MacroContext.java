package freemarker.core.variables.scope;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.Macro;
import freemarker.core.nodes.ParameterList;
import freemarker.core.nodes.generated.TemplateElement;
import freemarker.template.*;

/**
 * Represents the context or scope of the 
 * execution of an FTL macro. 
 */


public class MacroContext extends BlockScope {
    private Macro macro;
    private TemplateElement body; // REVISIT
    public ParameterList bodyParameters;
    private MacroContext invokingMacroContext;
    Scope invokingScope;
    
    public MacroContext(Macro macro,
    		Environment env,
            TemplateElement body,
            ParameterList bodyParameters)
    {
    	super(macro, env.getMacroNamespace(macro)); // REVISIT
    	this.macro = macro;
        this.invokingMacroContext = env.getCurrentMacroContext();
        this.invokingScope = env.getCurrentScope();
        this.body = body;
        this.bodyParameters = bodyParameters;
    }

    public MacroContext getInvokingMacroContext() {
        return invokingMacroContext;
    }

    public TemplateElement getBody() {
        return body;
    }
    
    public void runMacro() throws TemplateException, IOException { 
        TemplateElement nestedBlock = macro.firstChildOfType(TemplateElement.class);
        if (nestedBlock != null) {
            getEnvironment().render(nestedBlock);
        }
    }
}

