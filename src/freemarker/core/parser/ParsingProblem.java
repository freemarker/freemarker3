package freemarker.core.parser;
import freemarker.core.parser.ast.BaseNode;

/**
 * An object that encapsulates a problem that occurs 
 * when parsing a template. 
 * @author revusky
 */

public class ParsingProblem extends BaseNode {
	
	private String description;
	
	
	
	public ParsingProblem(String description) {
		this.description = description;
	}
	
	public ParsingProblem(String description, BaseNode location) {
		this.description = description;
		this.copyLocationFrom(location);
	}
	
	public String getDescription() {
		return description;
	}
}
