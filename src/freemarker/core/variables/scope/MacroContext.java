package freemarker.core.variables.scope;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.Block;
import freemarker.core.nodes.generated.Macro;
import freemarker.core.nodes.ParameterList;

/**
 * Represents the context or scope of the 
 * execution of an FTL macro. 
 */
public class MacroContext extends BlockScope {
    private Block body; 
    private ParameterList bodyParameters;
    private MacroContext invokingMacroContext;
    private Scope invokingScope;
    
    public MacroContext(Macro macro, Environment env, Block body, ParameterList bodyParameters) {
    	super(macro.getNestedBlock(), env.getMacroNamespace(macro)); // REVISIT
        this.invokingMacroContext = env.getCurrentMacroContext();
        this.invokingScope = env.getCurrentScope();
        this.body = body;
        this.bodyParameters = bodyParameters;
    }

    public ParameterList getBodyParameters() {
        return bodyParameters;
    }

    public Scope getInvokingScope() {
        return invokingScope;
    }

    public MacroContext getInvokingMacroContext() {
        return invokingMacroContext;
    }

    public Block getBody() {
        return body;
    }
}

