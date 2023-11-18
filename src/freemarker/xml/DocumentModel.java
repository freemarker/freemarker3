package freemarker.xml; 
 
import org.w3c.dom.*;
import freemarker.core.Environment;
import static freemarker.xml.ElementModel.isXMLID;

/**
 * A class that wraps the root node of a parsed XML document, using
 * the W3C DOM API.
 */

class DocumentModel extends WrappedDomNode {
    
    private ElementModel rootElement;
    
    DocumentModel(Document doc) {
        super(doc);
    }
    
    public String getNodeName() {
        return "@document";
    }
    
    public Object get(String key) {
        if (key.equals("*")) {
            return getRootElement();
        }
        else if (key.equals("**")) {
            NodeList nl = ((Document)node).getElementsByTagName("*");
            return new NodeListModel(nl, this);
        }
        else if (isXMLID(key)) {
            ElementModel em = (ElementModel) WrappedDomNode.wrapNode(((Document) node).getDocumentElement());
            if (em.matchesName(key, Environment.getCurrentEnvironment())) {
                return em;
            } else {
                return new NodeListModel(this);
            }
        }
        return super.get(key);
    }
    
    ElementModel getRootElement() {
        if (rootElement == null) {
            rootElement = (ElementModel) wrapNode(((Document) node).getDocumentElement());
        }
        return rootElement;
    }
    
    public boolean isEmpty() {
        return false;
    }
} 