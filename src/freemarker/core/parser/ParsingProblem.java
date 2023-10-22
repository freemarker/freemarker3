package freemarker.core.parser;
import freemarker.core.nodes.generated.TemplateNode;

/**
 * An object that encapsulates a problem that occurs 
 * when parsing a template. 
 * @author revusky
 */

public class ParsingProblem extends TemplateNode {
	
	private String description;
	
	
	
	public ParsingProblem(String description) {
		this.description = description;
	}
	
	public ParsingProblem(String description, TemplateNode location) {
		this.description = description;
		this.copyLocationFrom(location);
	}
	
	public String getDescription() {
		return description;
	}
}
