
 package freemarker3.xml;

import org.w3c.dom.*;
import freemarker3.template.*;

class PINodeModel extends WrappedDomNode {
    
    public PINodeModel(ProcessingInstruction pi) {
        super(pi);
    }
    
    public String toString() {
        return ((ProcessingInstruction) node).getData();
    }
    
    public String getNodeName() {
        return "@pi$" + ((ProcessingInstruction) node).getTarget();
    }
    
    public boolean isEmpty() {
        return true;
    }
}