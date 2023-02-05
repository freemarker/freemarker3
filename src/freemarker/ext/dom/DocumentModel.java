
 package freemarker.ext.dom; 
 
import org.w3c.dom.*;
import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.template.utility.StringUtil;

/**
 * A class that wraps the root node of a parsed XML document, using
 * the W3C DOM API.
 */

class DocumentModel extends NodeModel {
    
    private ElementModel rootElement;
    
    DocumentModel(Document doc) {
        super(doc);
    }
    
    public String getNodeName() {
        return "@document";
    }
    
    public TemplateModel get(String key) {
        if (key.equals("*")) {
            return getRootElement();
        }
        else if (key.equals("**")) {
            NodeList nl = ((Document)node).getElementsByTagName("*");
            return new NodeListModel(nl, this);
        }
        else if (StringUtil.isXMLID(key)) {
            ElementModel em = (ElementModel) NodeModel.wrap(((Document) node).getDocumentElement());
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
            rootElement = (ElementModel) wrap(((Document) node).getDocumentElement());
        }
        return rootElement;
    }
    
    public boolean isEmpty() {
        return false;
    }
} 