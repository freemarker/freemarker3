package freemarker.core;

import freemarker.core.ast.TemplateElement;
import freemarker.template.Template;
import freemarker.template.TemplateModel;


public class BlockContext extends BaseContext {
	
	
	TemplateElement block;
	
	public BlockContext(TemplateElement block, Scope enclosingScope) {
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
	
	public boolean definesVariable(String name) {
		return getBlock().declaresScopedVariable(name);
	}

	public TemplateElement getBlock() {
		return block;
	}
}

