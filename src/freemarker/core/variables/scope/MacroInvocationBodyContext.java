package freemarker.core.variables.scope;

import java.util.*;

import freemarker.core.Environment;
import freemarker.core.nodes.ParameterList;
import freemarker.core.nodes.generated.PositionalArgsList;
import freemarker.core.nodes.generated.TemplateElement;

/**
 * Represents the context or scope when a macro executes
 * the body of a macro invocation via [#nested]
 */

public class MacroInvocationBodyContext extends BlockScope {
    private MacroContext invokingMacroContext;
    private TemplateElement enclosingDirective;
    
    public MacroInvocationBodyContext(Environment env, PositionalArgsList bodyArgs) {
    	super(null, env.getCurrentMacroContext().invokingScope);
        invokingMacroContext = env.getCurrentMacroContext();
        block = invokingMacroContext.getBody().getNestedBlock();
        if (invokingMacroContext.getBody() != null) {
        	enclosingDirective = (TemplateElement) invokingMacroContext.getBody().getParent();
        }
        ParameterList bodyParameters = invokingMacroContext.bodyParameters;
        if (bodyParameters != null) {
            Map<String, Object> bodyParamsMap = bodyParameters.getParameterMap(bodyArgs, env, true);
            for (Map.Entry<String, Object> entry : bodyParamsMap.entrySet()) {
            	put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    public boolean definesVariable(String name) {
    	return enclosingDirective.getNestedBlock().declaresVariable(name);
    }
}
