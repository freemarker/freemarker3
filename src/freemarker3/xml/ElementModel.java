
package freemarker3.xml;

import java.util.List;

import org.w3c.dom.*;
import freemarker3.template.*;
import freemarker3.core.Environment;
import freemarker3.core.variables.EvaluationException;
import freemarker3.core.variables.WrappedNode;

class ElementModel extends WrappedDomNode {
    
    public ElementModel(Element element) {
        super(element);
    }
    
    /**
     * An Element node supports various hash keys.
     * Any key that corresponds to the tag name of any child elements
     * returns a sequence of those elements. The special key "*" returns 
     * all the element's direct children.
     * The "**" key return all the element's descendants in the order they
     * occur in the document.
     * Any key starting with '@' is taken to be the name of an element attribute.
     * The special key "@@" returns a hash of all the element's attributes.
     * The special key "/" returns the root document node associated with this element.
     */
    public Object get(String key) {
        if (key.equals("*")) {
            NodeListModel ns = new NodeListModel(this);
            List<WrappedNode> children = getChildNodes();
            for (int i=0;i < children.size();i++) {
                WrappedDomNode child = (WrappedDomNode) children.get(i);
                if (child.node.getNodeType() == Node.ELEMENT_NODE) {
                    ns.add(child);
                }
            }
            return ns;
        }
        if (key.equals("**")) {
            Element elem = (Element) node;
            return new NodeListModel(elem.getElementsByTagName("*"), this);    
        }
        if (key.startsWith("@")) {
            if (key.equals("@@") || key.equals("@*")) {
                return new NodeListModel(node.getAttributes(), this);
            }
            if (key.equals("@@start_tag")) {
                NodeOutputter nodeOutputter = new NodeOutputter(node);
                return nodeOutputter.getOpeningTag((Element) node);
            }
            if (key.equals("@@end_tag")) {
                NodeOutputter nodeOutputter = new NodeOutputter(node);
                return nodeOutputter.getClosingTag((Element) node);
            }
            if (key.equals("@@attributes_markup")) {
                StringBuilder buf = new StringBuilder();
                NodeOutputter nu = new NodeOutputter(node);
                nu.outputContent(node.getAttributes(), buf);
                return buf.toString().trim();
            }
            if (isXMLID(key.substring(1))) {
                Attr att = getAttribute(key.substring(1), Environment.getCurrentEnvironment());
                if (att == null) { 
                    return new NodeListModel(this);
                }
                return wrapNode(att);
            }
        }
        if (isXMLID(key)) {
            Environment env = Environment.getCurrentEnvironment();
            NodeListModel result = new NodeListModel(this);
            for (WrappedNode node : getChildNodes()) {
                if (node instanceof ElementModel) {
                    if (((ElementModel) node).matchesName(key, env)) {
                        result.add(node);
                    }                    
                }
            }
            if (result.size() == 1) {
                return result.get(0);
            }
            return result;
        }
        return super.get(key);
    }

    public String toString() {
        NodeList nl = node.getChildNodes();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i<nl.getLength(); i++) {
            Node child = nl.item(i);
            int nodeType = child.getNodeType();
            if (nodeType == Node.ELEMENT_NODE) {
                String msg = "Only elements with no child elements can be processed as text."
                             + "\nThis element with name \""
                             + node.getNodeName()
                             + "\" has a child element named: " + child.getNodeName();
                throw new EvaluationException(msg);
            }
            else if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
                result.append(child.getNodeValue());
            }
        }
        return result.toString();
    }
    
    public String getNodeName() {
        String result = node.getLocalName();
        if (result == null || result.equals("")) {
            result = node.getNodeName();
        }
        return result;
    }
    
    String getQualifiedName() {
        String nodeName = getNodeName();
        String nsURI = getNodeNamespace();
        if (nsURI == null || nsURI.length() == 0) {
            return nodeName;
        }
        Environment env = Environment.getCurrentEnvironment();
        String defaultNS = env.getDefaultNS();
        String prefix;
        if (defaultNS != null && defaultNS.equals(nsURI)) {
            prefix = Template.DEFAULT_NAMESPACE_PREFIX;
        } else {
            prefix = env.getPrefixForNamespace(nsURI);
            
        }
        if (prefix == null) {
            return null; // We have no qualified name, because there is no prefix mapping
        }
        if (prefix.length() >0) {
            prefix += ":";
        }
        return prefix + nodeName;
    }
    
    private Attr getAttribute(String qname, Environment env) {
        Element element = (Element) node;
        Attr result = element.getAttributeNode(qname);
        if (result != null)
            return result;
        int colonIndex = qname.indexOf(':');
        if (colonIndex >0) {
            String prefix = qname.substring(0, colonIndex);
            String uri;
            if (prefix.equals(Template.DEFAULT_NAMESPACE_PREFIX)) {
                uri = Environment.getCurrentEnvironment().getDefaultNS();
            } else {
                uri = Environment.getCurrentEnvironment().getNamespaceForPrefix(prefix);
            }
            String localName = qname.substring(1+colonIndex);
            if (uri != null) {
                result = element.getAttributeNodeNS(uri, localName);
            }
        }
        return result;
    }
    
    boolean matchesName(String name, Environment env) {
        return matchesName(name, getNodeName(), getNodeNamespace(), env);
    }

    /**
     * @return whether the qname matches the combination of nodeName, nsURI, and environment prefix settings. 
     */
    
    private static boolean matchesName(String qname, String nodeName, String nsURI, Environment env) {
        String defaultNS = env.getDefaultNS();
        if ((defaultNS != null) && defaultNS.equals(nsURI)) {
            return qname.equals(nodeName) 
               || qname.equals(Template.DEFAULT_NAMESPACE_PREFIX + ":" + nodeName); 
        }
        if ("".equals(nsURI)) {
            if (defaultNS != null) {
                return qname.equals(Template.NO_NS_PREFIX + ":" + nodeName);
            } else {
                return qname.equals(nodeName) || qname.equals(Template.NO_NS_PREFIX + ":" + nodeName);
            }
        }
        String prefix = env.getPrefixForNamespace(nsURI);
        if (prefix == null) {
            return false; // Is this the right thing here???
        }
        return qname.equals(prefix + ":" + nodeName);
    }

    /**
     * @return whether the name is a valid XML tagname.
     * (This routine might only be 99% accurate. Should maybe REVISIT) 
     */
    static boolean isXMLID(String name) {
        for (int i=0; i<name.length(); i++) {
            char c = name.charAt(i);
            if (i==0) {
                if (c== '-' || c=='.' || Character.isDigit(c))
                    return false;
            }
            if (!Character.isLetterOrDigit(c) && c != ':' && c != '_' && c != '-' && c!='.') {
                return false;
            }
        }
        return true;
    }
}