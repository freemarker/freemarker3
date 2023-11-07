package freemarker.xml;

import freemarker.core.variables.*;
import freemarker.core.Environment;

import org.w3c.dom.*;
import java.util.List; 
import java.util.ArrayList;
import static freemarker.xml.ElementModel.isXMLID;

class NodeListModel implements WrappedSequence, WrappedHash {

    NodeModel contextNode;
    XPathSupport xpathSupport;
    private List<Object> list = new ArrayList<>();
    
    NodeListModel(NodeModel contextNode) {
        this.contextNode = contextNode;
    }
    
    NodeListModel(NodeList nodeList, NodeModel contextNode) {
        for (int i=0; i<nodeList.getLength();i++) {
            list.add(nodeList.item(i));
        }
        this.contextNode = contextNode;
    }
    
    NodeListModel(NamedNodeMap nodeList, NodeModel contextNode) {
        for (int i=0; i<nodeList.getLength();i++) {
            list.add(nodeList.item(i));
        }
        this.contextNode = contextNode;
    }
    
    NodeListModel(List<Object> list, NodeModel contextNode) {
        this.list = list;
        this.contextNode = contextNode;
    }

    public void add(Object obj) {
        list.add(obj);
    }

    public int size() {
        return list.size();
    }

    public Object get(int i) {
        return list.get(i);
    }
    
    NodeListModel filterByName(String name) {
        NodeListModel result = new NodeListModel(contextNode);
        int size = size();
        if (size == 0) {
            return result;
        }
        Environment env = Environment.getCurrentEnvironment();
        for (int i = 0; i<size; i++) {
            NodeModel nm = (NodeModel) get(i);
            if (nm instanceof ElementModel) {
                if (((ElementModel) nm).matchesName(name, env)) {
                    result.add(nm);
                }
            }
        }
        return result;
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public Object get(String key) {
        if (size() ==1) {
            NodeModel nm = (NodeModel) get(0);
            return nm.get(key);
        }
        if (key.equals("@@markup") 
            || key.equals("@@nested_markup") 
            || key.equals("@@text"))
        {
            StringBuilder result = new StringBuilder();
            for (int i=0; i<size(); i++) {
                NodeModel nm = (NodeModel) get(i);
                Object textModel = nm.get(key);
                result.append(textModel.toString());
            }
            return result.toString();
        }
        if (isXMLID(key) 
            || ((key.startsWith("@") && isXMLID(key.substring(1))))
            || key.equals("*") || key.equals("**") || key.equals("@@") || key.equals("@*")) 
        {
            NodeListModel result = new NodeListModel(contextNode);
            for (int i=0; i<size(); i++) {
                NodeModel nm = (NodeModel) get(i);
                if (nm instanceof ElementModel) {
                    WrappedSequence tsm = (WrappedSequence) ((ElementModel) nm).get(key);
                    if(tsm != null) {
                        int size = tsm.size();
                        for (int j=0; j < size; j++) {
                            result.add(tsm.get(j));
                        }
                    }
                }
            }
            if (result.size() == 1) {
                return result.get(0);
            }
            return result;
        }
        XPathSupport xps = getXPathSupport();
        if (xps != null) {
            Object context = (size() == 0) ? null : rawNodeList(); 
            return xps.executeQuery(context, key);
        }
        throw new EvaluationException("Key: '" + key + "' is not legal for a node sequence ("
                + this.getClass().getName() + "). This node sequence contains " + size() + " node(s). "
                + "Some keys are valid only for node sequences of size 1. "
                + "If you use Xalan (instead of Jaxen), XPath expression keys work only with "
                + "node lists of size 1.");
    }
    
    private List<Node> rawNodeList() {
        int size = size();
        ArrayList<Node> al = new ArrayList<>(size);
        for (int i=0; i<size; i++) {
            al.add(((NodeModel) get(i)).node);
        }
        return al;
    }
    
    XPathSupport getXPathSupport() {
        if (xpathSupport == null) {
            if (contextNode != null) {
                xpathSupport = contextNode.getXPathSupport();
            }
            else if (size() >0) {
                xpathSupport = ((NodeModel) get(0)).getXPathSupport();
            }
        }
        return xpathSupport;
    }
}