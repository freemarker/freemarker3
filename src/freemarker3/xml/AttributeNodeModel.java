package freemarker3.xml;

import freemarker3.core.Environment;

import org.w3c.dom.*;

class AttributeNodeModel extends WrappedDomNode {
    
    public AttributeNodeModel(Attr att) {
        super(att);
    }
    
    public String toString() {
        return ((Attr) node).getValue();
    }
    
    public String getNodeName() {
        String result = node.getLocalName();
        if (result == null || result.equals("")) {
            result = node.getNodeName();
        }
        return result;
    }
    
    public boolean isEmpty() {
        return true;
    }
    
    @Override 
    String getQualifiedName() {
        String nsURI = node.getNamespaceURI();
        if (nsURI == null || nsURI.equals(""))
            return node.getNodeName();
        Environment env = Environment.getCurrentEnvironment();
        String defaultNS = env.getDefaultNS();
        String prefix = null;
        if (nsURI.equals(defaultNS)) {
            prefix = "D";
        } else {
            prefix = env.getPrefixForNamespace(nsURI);
        }
        if (prefix == null) {
            return null;
        }
        return prefix + ":" + node.getLocalName();
    }
}