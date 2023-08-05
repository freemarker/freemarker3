package freemarker.core.ast;

import java.util.*;
import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.core.parser.ast.TemplateNode;


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
    
    public Macro() {
    }
    
    public void setName(String name) {
    	this.name = name;
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

    public void execute(Environment env) {
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
