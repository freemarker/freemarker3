/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.core.ast;

import java.security.CodeSource;
import java.util.*;
import freemarker.template.*;
import freemarker.template.utility.UndeclaredThrowableException;
import freemarker.core.Environment;
import freemarker.core.parser.ParseException;


/**
 * An element representing a macro declaration.
 */
public final class Macro extends TemplateElement implements TemplateModel, Cloneable {
    private String name;
    private ParameterList params;
    boolean isFunction;
    static public final Macro DO_NOTHING_MACRO = new Macro();
    static {
    	DO_NOTHING_MACRO.setName(".pass");
    	DO_NOTHING_MACRO.setNestedBlock(TextBlock.EMPTY_BLOCK);
    }
    
    private Map<String, Macro> nestedMacros;
    private Macro parentMacro;
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
    
    public void setParams(ParameterList params) throws ParseException {
    	this.params = params;
    	for (String paramName : params.params) {
    		declareScopedVariable(paramName);
    	}
    	String catchallVar = params.getCatchAll();
    	if (catchallVar != null) {
    		declareScopedVariable(catchallVar);
    	}
    }
    
    public ParameterList getParams() {
    	return params;
    }
    
    public Macro getParentMacro() {
    	return parentMacro;
    }
    
    public boolean isFunction() {
    	return this.isFunction;
    }
    
    public void setFunction(boolean b) {
    	this.isFunction = b;
    }
    
    public void addNestedMacro(Macro nestedMacro) {
    	if (nestedMacros == null) nestedMacros = new HashMap<String, Macro>();
    	nestedMacros.put(nestedMacro.getName(), nestedMacro);
    	nestedMacro.parentMacro = this;
    }

    public String getName() {
        return name;
    }

    public void execute(Environment env) {
        env.visitMacroDef(this);
    }

    public String getDescription() {
        return "macro " + name;
    }
    
    public void canonicalizeAssignments() {
        if (createsScope() && (nestedBlock instanceof MixedContent)) {
            MixedContent block = (MixedContent) nestedBlock;
            VarDirective scopedDirective = null;
            Set<String> declaredVariables = new HashSet<String>();
            declaredVariables.addAll(params.getParamNames());
            for (TemplateElement te : block.getNestedElements()) {
                if (te instanceof VarDirective) {
                    VarDirective sdir = (VarDirective) te; 
                    if (scopedDirective == null){
                        scopedDirective = sdir;
                    }
                    Map<String, Expression> vars = sdir.getVariables();
                    for (String varname : vars.keySet()) {
                        declaredVariables.add(varname);
                    }
                }
            }
            for (String varname : scopedVariables) {
                if (!declaredVariables.contains(varname)) {
                    if (scopedDirective == null) {
                        scopedDirective = new VarDirective();
                        block.prependElement(scopedDirective);
                    }
                    scopedDirective.addVar(varname);
                }
            }
        }
    }
}
