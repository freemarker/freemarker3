package freemarker.core;

import java.io.IOException;

import freemarker.core.ast.*;
import freemarker.template.*;

/**
 * Represents the context or scope of the 
 * execution of an FTL macro. 
 */


public class MacroContext extends BlockScope {
    private Macro macro;
    TemplateElement body; // REVISIT
    public ParameterList bodyParameters;
    MacroContext invokingMacroContext;
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
    
    void runMacro() throws TemplateException, IOException { 
        TemplateElement nestedBlock = macro.getNestedBlock();
        if (nestedBlock != null) {
            getEnvironment().renderSecurely(nestedBlock, macro.getCodeSource());
        }
    }
}

