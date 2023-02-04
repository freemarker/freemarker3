
 package freemarker.ext.dom;

import org.w3c.dom.*;
import freemarker.template.*;

class PINodeModel extends NodeModel implements TemplateScalarModel {
    
    public PINodeModel(ProcessingInstruction pi) {
        super(pi);
    }
    
    public String getAsString() {
        return ((ProcessingInstruction) node).getData();
    }
    
    public String getNodeName() {
        return "@pi$" + ((ProcessingInstruction) node).getTarget();
    }
    
    public boolean isEmpty() {
        return true;
    }
}