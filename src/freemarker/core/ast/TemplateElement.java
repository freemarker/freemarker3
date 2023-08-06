package freemarker.core.ast;

import java.util.*;
import java.io.IOException;
import freemarker.core.*;
import freemarker.core.parser.ast.TemplateNode;

/**
 * Objects that represent elements in the compiled tree
 * representation of the template * descend from this abstract class.
 */
abstract public class TemplateElement extends TemplateNode {
	
    /**
     * Processes the contents of this <tt>TemplateElement</tt> and
     * outputs the resulting text
     *
     * @param env The runtime environment
     */
    abstract public void execute(Environment env) throws IOException;

    // The scoped variables defined in this element.
    private HashSet<String> declaredVariables;

    public Set<String> declaredVariables() {
        return declaredVariables;
    }

    public boolean declaresVariable(String name) {
    	return declaredVariables != null && declaredVariables.contains(name);
    }
    
    public void declareVariable(String varName) {
    	if (declaredVariables == null) declaredVariables = new HashSet<String>();
    	declaredVariables.add(varName);
    }
    
    public boolean isIgnorable() {
        return false;
    }

    public final boolean createsScope() {
    	return declaredVariables != null && !declaredVariables.isEmpty();
    }
}
