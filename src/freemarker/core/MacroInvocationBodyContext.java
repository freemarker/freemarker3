package freemarker.core;

import java.util.*;

import freemarker.core.ast.*;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * Represents the context or scope when a macro executes
 * the body of a macro invocation via [#nested]
 */

public class MacroInvocationBodyContext extends BlockScope {
    MacroContext invokingMacroContext;
    TemplateElement enclosingDirective;
    
    public MacroInvocationBodyContext(Environment env, PositionalArgsList bodyArgs) throws TemplateException {
    	super(null, env.getCurrentMacroContext().invokingScope);
        invokingMacroContext = env.getCurrentMacroContext();
        block = invokingMacroContext.body;
        if (invokingMacroContext.body != null) {
        	enclosingDirective = invokingMacroContext.body.getParent();
        }
        ParameterList bodyParameters = invokingMacroContext.bodyParameters;
        if (bodyParameters != null) {
            Map<String, TemplateModel> bodyParamsMap = bodyParameters.getParameterMap(bodyArgs, env, true);
            for (Map.Entry<String, TemplateModel> entry : bodyParamsMap.entrySet()) {
            	put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    public boolean definesVariable(String name) {
    	return enclosingDirective.declaresVariable(name);
    }
}
