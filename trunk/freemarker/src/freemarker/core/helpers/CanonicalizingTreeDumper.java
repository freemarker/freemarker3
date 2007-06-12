package freemarker.core.helpers;

import java.util.List;

import freemarker.core.ast.*;

public class CanonicalizingTreeDumper extends DefaultTreeDumper {
	
	boolean convertAssignments = true, convertExistence = true;
    
	public CanonicalizingTreeDumper() {
        super(true);
    }
    
    public CanonicalizingTreeDumper(boolean altSyntax) {
        super(altSyntax);
    }
    
    
    public String render(Macro macro) {
    	if (convertAssignments) {
    		macro.canonicalizeAssignments();
    	}
        return super.render(macro);
    }
    
	public String render(MethodCall node) {
		if (node.target instanceof BuiltIn) {
			BuiltIn bi = (BuiltIn) node.target;
			if (bi.getName().equals("default")) { // convert ?default to newer syntax
				Expression lhs = bi.getTarget();
				StringBuilder buf = new StringBuilder();
				buf.append(render(lhs));
				ArgsList args = node.getArgs();
				if (args instanceof PositionalArgsList) {
					PositionalArgsList pargs = (PositionalArgsList) args;
					for (Expression arg : pargs.getArgs()) {
						buf.append("!");
						buf.append(render(arg));
					}
					return buf.toString();
				}
			}
		}
		return super.render(node);
	}
	

    public String render(BuiltIn node) {
    	if (!convertExistence) {
    		return super.render(node);
    	}
        String builtinName = node.getName();
        if (builtinName.equals("if_exists")) {
            return render(node.getTarget()) + "!";
        }
        if (builtinName.equals("exists")) {
        	return render(node.getTarget()) + "??";
        }
        return render(node.getTarget()) + "?" + builtinName;
    }
    
    public String render(AssignmentInstruction node) {
    	if (!convertAssignments) {
    		return super.render(node);
    	}
        StringBuilder buf = new StringBuilder();
        buf.append(OPEN_BRACKET);
        buf.append("#set ");
        if (node.type == AssignmentInstruction.GLOBAL) {
            
        }
        List<String> varnames = node.getVarNames();
        List<Expression> values = node.getValues();
        for (int i=0; i<varnames.size(); i++) {
            if (i>0) buf.append(", ");
            String varname = varnames.get(i);
            Expression value = values.get(i);
            buf.append(quoteVarnameIfNecessary(varname));
            buf.append(" = ");
            buf.append(render(value));
        }
        Expression namespaceExp = node.getNamespaceExp();
        if (namespaceExp != null) {
            buf.append (" in ");
            buf.append(render(namespaceExp));
        } else {
            if (node.type == AssignmentInstruction.GLOBAL) {
                buf.append (" in .globals");
            }
            if (node.type == AssignmentInstruction.NAMESPACE) {
                Macro enclosingMacro = node.getEnclosingMacro();
                if (enclosingMacro != null) {
                    boolean declaredLocally = false;
                    for (String varname : varnames) {
                        if (enclosingMacro.declaresScopedVariable(varname)) {
                            declaredLocally = true;
                        }
                    }
                    if (declaredLocally) {
                        buf.append (" in .namespace");
                    }
                }
            }
        }
        buf.append(CLOSE_BRACKET);
        return buf.toString();
    }
}
