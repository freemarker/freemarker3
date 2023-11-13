package freemarker.xml;

import freemarker.core.Environment;
import freemarker.template.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static freemarker.core.variables.Wrap.JAVA_NULL;

import java.util.Iterator;
import java.util.LinkedList;

// This class is really ghastly!
// TODO: Consider caching the compiled XPath expressions, I guess.

public class DefaultXPathSupport implements XPathSupport {

    static private XPathFactory xpf;
    static DefaultXPathSupport instance = new DefaultXPathSupport();

    static {
        xpf = XPathFactory.newInstance();
    }

    public Object executeQuery(Object context, String xpathQuery) {
        XPath xp = null;
        synchronized(xpf) {
            xp = xpf.newXPath();
        }
        xp.setNamespaceContext(nsc);
        try {
            NodeList nl = (NodeList) xp.evaluate(xpathQuery, context, XPathConstants.NODESET);
            if (nl.getLength() == 1){
                Node n = nl.item(0);
                return NodeModel.wrapNode(n);
            }
            return new NodeListModel(nl, null);
        } catch (Exception e) {
            //Ignore this, I guess.
            //If this fails, we just run the query again asking for a string.
            //Terribly kludgy and inefficient, but it seems to work...
        }
        String scalar = null;
        try {
            scalar = xp.evaluate(xpathQuery, context);
        } catch (Exception e) {}
        if (scalar == null) {
        	// I'm not even sure that this can be null, but just in case...
        	return JAVA_NULL;
        }
        // Man, this whole XPATH API was created by total morons!
        if (scalar.equals("true")) return Boolean.TRUE;
        if (scalar.equals("false")) return Boolean.FALSE;
        try {
            //return new XPathNumber(Double.valueOf(scalar));
            return Double.valueOf(scalar);
        } catch (Exception e) {
            // Ignore this exception too.
        }
        // Well, if we can't parse it as a bool or number, then it's just a string, I guess!
        return scalar;
    }

    private static NamespaceContext nsc = new NamespaceContext() {
        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix.equals(Template.DEFAULT_NAMESPACE_PREFIX)) {
                return Environment.getCurrentEnvironment().getDefaultNS();
            }
            return Environment.getCurrentEnvironment().getNamespaceForPrefix(prefix);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return Environment.getCurrentEnvironment().getPrefixForNamespace(namespaceURI);
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            String prefix = getPrefix(namespaceURI);
            LinkedList l = new LinkedList();
            l.add(prefix);
            return l.iterator();
        }
    };
}

