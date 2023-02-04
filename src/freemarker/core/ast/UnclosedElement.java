package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

/**
 * An AST node representing an element that is missing 
 * its mandatory closing tag or brace
 * @author revusky
 */

public class UnclosedElement extends TemplateElement {
	
	private String description;
	
	public UnclosedElement(String description) {
		this.description = description;
	}

	@Override
	public void execute(Environment env) throws TemplateException, IOException {
		throw new TemplateException(description, env); // We should typically have failed before this point anyway. 
	}
	
	
	public String getDescription() {
		return description;
	}
}
