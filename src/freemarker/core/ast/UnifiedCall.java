package freemarker.core.ast;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.core.parser.ast.Expression;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateTransformModel;

/**
 * An element for the unified macro/transform syntax. 
 */
public class UnifiedCall extends TemplateElement {

    private Expression nameExp;
    private ArgsList args;
    private ParameterList bodyParameters;
    
    public UnifiedCall() {}
    
    public ArgsList getArgs() {
    	return args;
    }
    
    public void setArgs(ArgsList args) {
    	this.args = args;
    	args.setParent(this);
    }
    
    public ParameterList getBodyParameters() {
    	return bodyParameters;
    }
    
    public void setNameExp(Expression nameExp) {
    	this.nameExp = nameExp;
    }
    
    public Expression getNameExp() {
    	return nameExp;
    }
    
    public void setBodyParameters(ParameterList bodyParameters) {
    	this.bodyParameters = bodyParameters;
    	if (bodyParameters != null) {
    		for (String paramName : bodyParameters.params) {
    			declareVariable(paramName);
    		}
    		String catchallParam = bodyParameters.getCatchAll();
    		if (catchallParam != null) {
    			declareVariable(catchallParam);
    		}
    	}
    }

    public void execute(Environment env) throws IOException {
        Object tm = nameExp.evaluate(env);
        if (tm == Macro.DO_NOTHING_MACRO) return; // shortcut here.
        if (tm instanceof Macro) {
            Macro macro = (Macro) tm;
            if (macro.isFunction()) {
                throw new TemplateException("Routine " + macro.getName() 
                        + " is a function. A function can only be called " +
                        "within the evaluation of an expression.", env);
            }    
            env.render(macro, args, bodyParameters, firstChildOfType(TemplateElement.class));
        }
        else if (tm instanceof TemplateDirectiveModel) {
            Map<String, Object> argMap
                    = args != null
                            ? args.getParameterMap(tm, env)
                            : new HashMap<String, Object>();
            List<String> paramNames;
            if(bodyParameters == null) {
                paramNames = Collections.emptyList();
            }
            else {
                paramNames = bodyParameters.getParamNames();
            }
            env.render(firstChildOfType(TemplateElement.class), (TemplateDirectiveModel) tm, argMap, paramNames);
        }
        else if (tm instanceof TemplateTransformModel) {
            Map<String, Object> argMap
                    = args != null
                            ? args.getParameterMap(tm, env)
                            : new HashMap<String, Object>();
            env.render(firstChildOfType(TemplateElement.class), (TemplateTransformModel) tm, argMap);
        }
        else {
            assertNonNull(tm, nameExp, env);
            throw new TemplateException(getStartLocation() + ": " + nameExp + 
                    " is not a user-defined directive.", env);
        }
    }
    
    
    public String getDescription() {
        return "user-directive " + nameExp;
    }
}
