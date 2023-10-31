package freemarker.core.variables.scope;

import freemarker.template.*;

/**
 * Represents the namespace associated with a template 
 * @author Jonathan Revusky
 */

public class TemplateNamespace extends BaseScope {
	
	Template template;
	
	public TemplateNamespace(Scope env, Template template) {
		super(env);
		this.template = template;
	}
	
	public Template getTemplate() {
		return template;
	}

	public void put(String name, Object var) {
		if (template.strictVariableDeclaration() && !template.declaresVariable(name)) {
			throw new UndeclaredVariableException("Cannot set variable " + name + " since it is not declared.");
		}
		super.put(name, var);
	}
	
	public boolean definesVariable(String name) {
		return template.declaresVariable(name);
	}
}
