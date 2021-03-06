/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package freemarker.core.ast;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
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
    	args.parent = this;
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

    public void execute(Environment env) throws TemplateException, IOException {
        TemplateModel tm = nameExp.getAsTemplateModel(env);
        if (tm == Macro.DO_NOTHING_MACRO) return; // shortcut here.
        if (tm instanceof Macro) {
            Macro macro = (Macro) tm;
            if (macro.isFunction()) {
                throw new TemplateException("Routine " + macro.getName() 
                        + " is a function. A function can only be called " +
                        "within the evaluation of an expression.", env);
            }    
            env.render(macro, args, bodyParameters, nestedBlock);
        }
        else if (tm instanceof TemplateDirectiveModel) {
            Map<String, TemplateModel> argMap
                    = args != null
                            ? args.getParameterMap(tm, env)
                            : new HashMap<String, TemplateModel>();
            List<String> paramNames;
            if(bodyParameters == null) {
                paramNames = Collections.emptyList();
            }
            else {
                paramNames = bodyParameters.getParamNames();
            }
            env.render(nestedBlock, (TemplateDirectiveModel) tm, argMap, paramNames);
        }
        else if (tm instanceof TemplateTransformModel) {
            Map<String, TemplateModel> argMap
                    = args != null
                            ? args.getParameterMap(tm, env)
                            : new HashMap<String, TemplateModel>();
            env.render(nestedBlock, (TemplateTransformModel) tm, argMap);
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
