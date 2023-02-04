package freemarker.core;

import freemarker.core.ast.TemplateElement;
import freemarker.template.*;


public class BlockScope extends BaseScope {
	
	
	TemplateElement block;
	
	public BlockScope(TemplateElement block, Scope enclosingScope) {
		super(enclosingScope);
		this.block = block;
	}
	
	public Template getTemplate() {
		return block.getTemplate();
	}
	
	public void put(String key, TemplateModel tm) {
		if (!definesVariable(key)) {
			throw new IllegalArgumentException("The variable " + key + " is not declared here.");
		}
		super.put(key, tm);
	}
	
	public TemplateElement getBlock() {
		return block;
	}
	
	public boolean definesVariable(String name) {
		return getBlock().declaresVariable(name);
	}
}

