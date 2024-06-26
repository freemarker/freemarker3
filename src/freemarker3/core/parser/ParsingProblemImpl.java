package freemarker3.core.parser;
import freemarker3.core.nodes.generated.TemplateNode;

/**
 * An object that encapsulates a problem that occurs 
 * when parsing a template. 
 * @author revusky
 */

public class ParsingProblemImpl extends TemplateNode {
	
	private String description;
	
	public ParsingProblemImpl(String description, Node location) {
		this.description = description;
		this.copyLocationFrom(location);
	}
	
	public String getDescription() {
		return description;
	}

	public String getMessage() {
		return description + " " + getLocation();
	}
}
