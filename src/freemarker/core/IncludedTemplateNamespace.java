package freemarker.core;

import freemarker.template.*;

/**
 * This class is used for the scope of an included template.
 * If a variable is not found,in this namespace, it falls back
 * to the scope in the template from which this template was included. 
 * @author revusky
 */

public class IncludedTemplateNamespace extends TemplateNamespace {

	IncludedTemplateNamespace(Template template, Scope includingScope) {
		super(includingScope, template);
	}
	
	public void put(String name, TemplateModel var) {
		if (template.declaresVariable(name)) {
			super.put(name, var);
		}
		else {
			Scope scope = getEnclosingScope();
			while (!(scope instanceof TemplateNamespace) && !scope.definesVariable(name)) {
				scope = scope.getEnclosingScope();
			}
			scope.put(name, var);
		}
	}
}
