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
import java.util.*;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.core.*;

/**
 * An instruction that contains one or more assignments
 */
public class AssignmentInstruction extends TemplateElement {
	
	static public final int SET = 0;
	static public final int NAMESPACE = 1;
	static public final int GLOBAL = 2;
	static public final int LOCAL = 3;

    private int type; 
    private Expression namespaceExp;
    
    private ArrayList<String> varNames = new ArrayList<String>();
    private ArrayList<Expression> values = new ArrayList<Expression>();

    public AssignmentInstruction(int type) {
        this.type = type;
    }
    
    public int getType() {
    	return type;
    }
    
    public List<String> getVarNames() {
    	return Collections.unmodifiableList(varNames);
    }
    
    public List<Expression> getValues() {
    	return Collections.unmodifiableList(values);
    }

    public void addAssignment(String var, Expression valueExp) {
    	varNames.add(var);
    	values.add(valueExp);
    }
    
    public void setNamespaceExp(Expression namespaceExp) {
        this.namespaceExp = namespaceExp;
    }
    
    public Expression getNamespaceExp() {
        return namespaceExp;
    }

    public void execute(Environment env) throws TemplateException, IOException {
    	Scope scope = null;
    	if (namespaceExp != null) {
    		try {
    			scope = (Scope) namespaceExp.getAsTemplateModel(env); 
    		} catch (ClassCastException cce) {
                throw new InvalidReferenceException(getStartLocation() + "\nInvalid reference to namespace: " + namespaceExp, env);
    		}
    	}
    	else {
    		if (type == NAMESPACE) {
    			scope = env.getCurrentNamespace();
    		} else if (type == LOCAL) {
    			scope = env.getCurrentMacroContext();
    		} else if (type == GLOBAL) {
    			scope = env.getGlobalNamespace();
    		}
    	}
    	for (int i=0; i< varNames.size(); i++) {
    		String varname = varNames.get(i);
    		Expression valueExp = values.get(i);
    		TemplateModel value = valueExp.getAsTemplateModel(env);
    		assertIsDefined(value, valueExp, env);
    		if (scope != null) {
    			scope.put(varname, value);
    		} else {
    			env.unqualifiedSet(varname, value);
    		}
    	}
    }
    
    public String getDescription() {
    	return "assignment instruction";
    }
}
