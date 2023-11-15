package freemarker.core.variables.scope;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.Macro;
import freemarker.core.nodes.ParameterList;
import freemarker.core.nodes.generated.TemplateElement;

/**
 * Represents the context or scope of the 
 * execution of an FTL macro. 
 */
public class MacroContext extends BlockScope {
    private TemplateElement body; // REVISIT
    public ParameterList bodyParameters;
    private MacroContext invokingMacroContext;
    Scope invokingScope;
    
    public MacroContext(Macro macro,
    		Environment env,
            TemplateElement body,
            ParameterList bodyParameters)
    {
    	super(macro.getNestedBlock(), env.getMacroNamespace(macro)); // REVISIT
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
}

