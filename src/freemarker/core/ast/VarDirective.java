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

import java.util.*;
import freemarker.template.*;
import freemarker.core.*;

/**
 * @author Jonathan Revusky
 */
public class VarDirective extends TemplateElement {
    private Map<String, Expression> vars = new LinkedHashMap<String, Expression>();

    public void execute(Environment env) throws TemplateException {
        for (Map.Entry<String, Expression> entry : vars.entrySet()) {
            String varname = entry.getKey();
            Expression exp = entry.getValue();
            Scope scope = env.getCurrentScope();
            if (exp == null) {
                if (scope.get(varname) == null) {
                    scope.put(varname, TemplateModel.JAVA_NULL);
                }
            } 
            else {
                TemplateModel tm = exp.getAsTemplateModel(env);
                assertIsDefined(tm, exp, env);
                scope.put(varname, tm);
            }
        }
    }

    public Map<String, Expression> getVariables() {
        return Collections.unmodifiableMap(vars);
    }

    public void addVar(Expression name, Expression value) {
        String varname = name.toString();
        if (name instanceof StringLiteral) {
            varname = ((StringLiteral) name).getAsString();
        }
        vars.put(varname, value);
    }
    
    public void addVar(String name) {
        vars.put(name, null);
    }

    public String getDescription() {
        return "variable declaration";
    }
}