package freemarker.core.parser;

/**
 * An object that encapsulates a problem that occurs 
 * when parsing a template. 
 * @author revusky
 */

public class ParsingProblem extends TemplateLocation {
	
	private String description;
	
	
	
	public ParsingProblem(String description) {
		this.description = description;
	}
	
	public ParsingProblem(String description, TemplateLocation location) {
		this.description = description;
		this.copyLocationFrom(location);
	}
	
	public String getDescription() {
		return description;
	}
}
