package freemarker.core.variables.scope;

import java.util.HashMap;
import java.util.Map;

import freemarker.core.nodes.generated.Block;
import freemarker.template.*;


public class BlockScope extends AbstractScope {
	
	Block block;
    private Map<String,Object> variables = new HashMap<>();
	
	public BlockScope(Block block, Scope enclosingScope) {
		super(enclosingScope);
		this.block = block;
	}

    public Object get(String key) { 
        return variables.get(key);
    }
	
	public Template getTemplate() {
		return block.getTemplate();
	}
	
	public Object put(String key, Object tm) {
		if (getTemplate().strictVariableDeclaration() && !definesVariable(key)) {
			throw new IllegalArgumentException("The variable " + key + " is not declared here.");
		}
		return variables.put(key, tm);
	}

	protected void putUnconditionally(String key, Object var) {
		variables.put(key, var);
	}

	public Block getBlock() {
		return block;
	}
	
	public boolean definesVariable(String name) {
		return getBlock().declaresVariable(name);
	}

    public Object remove(String key) {
        return variables.remove(key);
    }

    public void clear() {
        variables.clear();
    }

	public boolean isTemplateNamespace() {
		return block.isTemplateRoot();
	}
}

