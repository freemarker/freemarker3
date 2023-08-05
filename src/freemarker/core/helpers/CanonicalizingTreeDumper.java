package freemarker.core.helpers;

import java.util.*;
import freemarker.core.ast.*;
import freemarker.core.parser.ast.BaseNode;
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
    		for (BaseNode te : node.getNestedElements()) {
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
            if (node.getBlockType() == AssignmentInstruction.GLOBAL) {
                buffer.append (" in .globals");
            }
            if (node.getBlockType() == AssignmentInstruction.NAMESPACE) {
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
