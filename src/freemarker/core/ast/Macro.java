package freemarker.core.ast;

import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.core.parser.ast.ParameterList;
import freemarker.core.parser.ast.TemplateElement;


/**
 * An element representing a macro declaration.
 */
public final class Macro extends TemplateElement implements TemplateModel {
    private String name;
    private ParameterList params;
    private boolean isFunction;
    static public final Macro DO_NOTHING_MACRO = new Macro();
    static {
    	DO_NOTHING_MACRO.setName(".pass");
    	DO_NOTHING_MACRO.add((TemplateElement) TextBlock.EMPTY_BLOCK);
    }
    
    public Macro() {
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    public void setParams(ParameterList params) {
    	this.params = params;
    	for (String paramName : params.getParams()) {
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
}
