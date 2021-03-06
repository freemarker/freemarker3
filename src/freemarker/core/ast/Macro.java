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

import java.security.CodeSource;
import java.util.*;
import freemarker.template.*;
import freemarker.template.utility.UndeclaredThrowableException;
import freemarker.core.Environment;


/**
 * An element representing a macro declaration.
 */
public final class Macro extends TemplateElement implements TemplateModel, Cloneable {
    private String name;
    private ParameterList params;
    private boolean isFunction;
    static public final Macro DO_NOTHING_MACRO = new Macro();
    static {
    	DO_NOTHING_MACRO.setName(".pass");
    	DO_NOTHING_MACRO.setNestedBlock(TextBlock.EMPTY_BLOCK);
    }
    
    private final CodeSource codeSource;
    
    public Macro() {
        codeSource = Template.NULL_CODE_SOURCE;
    }
    
    public Macro(CodeSource codeSource) {
        this.codeSource = codeSource;
    }
    
    public CodeSource getCodeSource() {
        return codeSource;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    Macro createCurriedMacro(ParameterList curriedParams, Environment env) {
        try {
            Macro curried = (Macro)super.clone();
            curried.params = curriedParams;
            env.setCurriedMacroNamespace(curried, this);
            return curried;
        } catch (CloneNotSupportedException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }
    
    public void setParams(ParameterList params) {
    	this.params = params;
    	for (String paramName : params.params) {
    		declareVariable(paramName);
    	}
    	String catchallVar = params.getCatchAll();
    	if (catchallVar != null) {
    		declareVariable(catchallVar);
    	}
    }
    
    public ParameterList getParams() {
    	return params;
    }
    
    public boolean isFunction() {
    	return this.isFunction;
    }
    
    public void setFunction(boolean b) {
    	this.isFunction = b;
    }
    
    public String getName() {
        return name;
    }

    public void execute(Environment env) throws TemplateException {
        env.visitMacroDef(this);
    }

    public String getDescription() {
        return (isFunction() ? "function " : "macro ") + name;
    }
    
    public void canonicalizeAssignments() {
        if (createsScope() && (nestedBlock instanceof MixedContent)) {
            MixedContent block = (MixedContent) nestedBlock;
            VarDirective varDirective = null;
            Set<String> variables = new HashSet<String>();
            variables.addAll(params.getParamNames());
            for (TemplateNode te : block.getNestedElements()) {
                if (te instanceof VarDirective) {
                    VarDirective sdir = (VarDirective) te; 
                    if (varDirective == null){
                        varDirective = sdir;
                    }
                    Map<String, Expression> vars = sdir.getVariables();
                    for (String varname : vars.keySet()) {
                        variables.add(varname);
                    }
                }
            }
            for (String varname : declaredVariables) {
                if (!variables.contains(varname)) {
                    if (varDirective == null) {
                        varDirective = new VarDirective();
                        block.prependElement(varDirective);
                    }
                    varDirective.addVar(varname);
                }
            }
        }
    }
}
