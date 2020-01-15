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

package freemarker.core.helpers;

import java.util.*;
import freemarker.core.ast.*;
import freemarker.template.Template;

/**
 * An object that transforms an FTL tree to convert older
 * syntactical elements to the newer improved ones in 2.4,
 * such as changing #assign/#local to #set, also changing
 * existence built-ins to their shorter form, and so on.  
 * @author revusky
 */

public class CanonicalizingTreeDumper extends DefaultTreeDumper {
	
	boolean convertAssignments = true, convertExistence = true, strictVars;
    
	public CanonicalizingTreeDumper() {
        super(true);
    }
    
    public CanonicalizingTreeDumper(boolean altSyntax) {
        super(altSyntax);
    }
    
    public String render(Template template) {
    	if (strictVars) {
    		TemplateHeaderElement header = template.getHeaderElement();
    		if (header == null) {
    			Map<String, Expression> params = new HashMap<String, Expression>();
    			params.put("strict_vars", new BooleanLiteral(true));
    			header = new TemplateHeaderElement(params);
    		} else {
    			header.addParameter("strict_vars", new BooleanLiteral(false));
    		}
    	}
    	return super.render(template);
    }
    
    
    public void visit(MixedContent node) {
    	if (strictVars && node.getParent() == null) { // the root node
    		Template template = node.getTemplate();
    		List<String> declaredVariables = new ArrayList<String>(template.getDeclaredVariables());
    		// Now we get rid of the ones that are already declared, either
    		// via var or via macro.
    		for (TemplateNode te : node) {
    			if (te instanceof Macro) {
    				String macroName = ((Macro) te).getName();
    				declaredVariables.remove(macroName);
    			}
    			if (te instanceof VarDirective) {
    				VarDirective varDirective = (VarDirective) te;
    				for (String varname : varDirective.getVariables().keySet()) {
    					declaredVariables.remove(varname);
    				}
    			}
    		}
    		if (!declaredVariables.isEmpty()) {
    			VarDirective varDirective = new VarDirective();
    			for (String varname : declaredVariables) {
    				varDirective.addVar(varname);
    			}
    			visit(varDirective);
    			buffer.append("\n");
    		}
       	}
		super.visit(node);
    }
    
    
    public void visit(Macro macro) {
    	if (convertAssignments) {
    		macro.canonicalizeAssignments();
    	}
    	super.visit(macro);
    }
    
	public void visit(MethodCall node) {
		if (convertExistence && (node.getTarget() instanceof BuiltInExpression)) {
			BuiltInExpression bi = (BuiltInExpression) node.getTarget();
			if (bi.getName().equals("default")) { // convert ?default to newer syntax
				Expression lhs = bi.getTarget();
				buffer.append(render(lhs));
				ArgsList args = node.getArgs();
				if (args instanceof PositionalArgsList) {
					PositionalArgsList pargs = (PositionalArgsList) args;
					for (Expression arg : pargs.getArgs()) {
						buffer.append("!");
						visit(arg);
					}
				}
				return;
			}
		} 
		super.visit(node);
	}
	

    public void visit(BuiltInExpression node) {
    	if (!convertExistence) {
    		super.visit(node);
    		return;
    	}
    	visit(node.getTarget());
        String builtinName = node.getName();
        if (builtinName.equals("if_exists")) {
            buffer.append("!");
        }
        else if (builtinName.equals("exists")) {
        	buffer.append("??");
        }
        else {
        	buffer.append("?");
        	buffer.append(builtinName);
        }
    }
    
    public void visit(AssignmentInstruction node) {
    	if (!convertAssignments) {
    		super.visit(node);
    		return;
    	}
    	openDirective("set ");
        List<String> varnames = node.getVarNames();
        List<Expression> values = node.getValues();
        for (int i=0; i<varnames.size(); i++) {
            if (i>0) buffer.append(", ");
            String varname = varnames.get(i);
            Expression value = values.get(i);
            buffer.append(quoteVarnameIfNecessary(varname));
            buffer.append(" = ");
            visit(value);
        }
        Expression namespaceExp = node.getNamespaceExp();
        if (namespaceExp != null) {
            buffer.append (" in ");
            visit(namespaceExp);
        } else {
            if (node.getType() == AssignmentInstruction.GLOBAL) {
                buffer.append (" in .globals");
            }
            if (node.getType() == AssignmentInstruction.NAMESPACE) {
                Macro enclosingMacro = node.getEnclosingMacro();
                if (enclosingMacro != null) {
                    boolean declaredLocally = false;
                    for (String varname : varnames) {
                        if (enclosingMacro.declaresVariable(varname)) {
                            declaredLocally = true;
                        }
                    }
                    if (declaredLocally) {
                        buffer.append (" in .namespace");
                    }
                }
            }
        }
        buffer.append(CLOSE_BRACKET);
    }
}
