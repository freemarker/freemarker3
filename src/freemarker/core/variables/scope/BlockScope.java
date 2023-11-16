package freemarker.core.variables.scope;

import java.util.HashMap;
import freemarker.core.nodes.generated.Block;
import freemarker.template.*;

public class BlockScope extends HashMap<String,Object> implements Scope {
	
	Block block;
	private Scope enclosingScope;
	
	public BlockScope(Block block, Scope enclosingScope) {
		this.block = block;
		this.enclosingScope = enclosingScope;
	}

	public Scope getEnclosingScope() {
		return enclosingScope;
	}

    public Object get(String key) { 
        return super.get(key);
    }
	
	public Template getTemplate() {
		return block.getTemplate();
	}
	
	public Object put(String key, Object tm) {
		if (getTemplate().strictVariableDeclaration() && !definesVariable(key)) {
			throw new IllegalArgumentException("The variable " + key + " is not declared here.");
		}
		return super.put(key, tm);
	}

	protected void putUnconditionally(String key, Object var) {
		super.put(key, var);
	}

	public Block getBlock() {
		return block;
	}
	
	public boolean definesVariable(String name) {
		return getBlock().declaresVariable(name);
	}

    public Object remove(String key) {
        return super.remove(key);
    }

    public void clear() {
        super.clear();
    }

	public boolean isTemplateNamespace() {
		return block.isTemplateRoot();
	}
}

