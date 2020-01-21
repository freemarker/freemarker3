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
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import freemarker.template.TemplateModel;
import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.Scope;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateException;
import freemarker.template.TemplateTransformModel;

/**
 * @version $Id: BlockAssignment.java,v 1.4 2004/07/07 21:11:12 szegedia Exp $
 */
@SuppressWarnings("deprecation")
public class BlockAssignment extends TemplateElement {

    private String varName;
    private Expression namespaceExp;
    private int type;

    public BlockAssignment(TemplateElement nestedBlock, String varName, int type, Expression namespaceExp) {
        this.nestedBlock = nestedBlock;
        this.varName = varName;
        this.namespaceExp = namespaceExp;
        this.type = type;
    }
    
    public Expression getNamespaceExpression() {
    	return namespaceExp;
    }
    
    public String getVarName() {
    	return varName;
    }
    
    public int getType() {
    	return type;
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
    		if (type == AssignmentInstruction.NAMESPACE) {
    			scope = env.getCurrentNamespace();
    		} else if (type == AssignmentInstruction.LOCAL) {
    			scope = env.getCurrentMacroContext();
    		} else if (type == AssignmentInstruction.GLOBAL) {
    			scope = env.getGlobalNamespace();
    		} 
    	}
    	CaptureOutput filter = new CaptureOutput();
        if (nestedBlock != null) {
            env.render(nestedBlock, filter, null);
        }
        String text = filter.capturedText;
    	if (scope != null) {
    		scope.put(varName, new SimpleScalar(text));
    	} else {
    		env.unqualifiedSet(varName, new SimpleScalar(text));
    	}
    }
    
    private static class CaptureOutput implements TemplateTransformModel {
        String capturedText = ""; 
        
        public Writer getWriter(Writer out, List args) {
        	return getWriter(out, (Map) null);
        }
        
        public Writer getWriter(Writer out, Map<String, TemplateModel> args) {
            return new StringWriter() {
                public void close() {
                	capturedText = this.toString();
                }
            };
        }
    }
    
    public String getDescription() {
        return "block assignment to variable: " + varName;
    }

}

