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
