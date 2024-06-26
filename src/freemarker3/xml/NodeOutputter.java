
 package freemarker3.xml;

import freemarker3.template.Template;
import freemarker3.core.Environment;

import java.util.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class NodeOutputter {
    
    private Element contextNode;
    private Environment env;
    private String defaultNS;
    private boolean hasDefaultNS;
    private boolean explicitDefaultNSPrefix;
    private HashMap<String, String> namespacesToPrefixLookup = new HashMap<String,String>();
    private String namespaceDecl;
    
    NodeOutputter(Node node) {
        if (node instanceof Element) {
            setContext((Element) node);
        }
        else if (node instanceof Attr) {
            setContext(((Attr) node).getOwnerElement());
        }
        else if (node instanceof Document) {
            setContext(((Document) node).getDocumentElement());
        }
    }
    
    private void setContext(Element contextNode) {
        this.contextNode = contextNode;
        this.env = Environment.getCurrentEnvironment();
        this.defaultNS = env.getDefaultNS();
        this.hasDefaultNS = defaultNS != null && defaultNS.length() >0;
        namespacesToPrefixLookup.put(null, "");
        namespacesToPrefixLookup.put("", "");
        buildPrefixLookup(contextNode);
        if (!explicitDefaultNSPrefix && hasDefaultNS) {
            namespacesToPrefixLookup.put(defaultNS, "");
        }
        constructNamespaceDecl();
    }
    
    private void buildPrefixLookup(Node n) {
        String nsURI = n.getNamespaceURI();
        if (nsURI != null && nsURI.length() >0) {
            String prefix = env.getPrefixForNamespace(nsURI);
            namespacesToPrefixLookup.put(nsURI, prefix);
        }  else if (hasDefaultNS && n.getNodeType() == Node.ELEMENT_NODE) {
            namespacesToPrefixLookup.put(defaultNS, Template.DEFAULT_NAMESPACE_PREFIX); 
            explicitDefaultNSPrefix = true;
        } else if (n.getNodeType() == Node.ATTRIBUTE_NODE && hasDefaultNS && defaultNS.equals(nsURI)) {
            namespacesToPrefixLookup.put(defaultNS, Template.DEFAULT_NAMESPACE_PREFIX); 
            explicitDefaultNSPrefix = true;
        }
        NodeList childNodes = n.getChildNodes();
        for (int i = 0; i<childNodes.getLength(); i++) {
            buildPrefixLookup(childNodes.item(i));
        }
    }
    
    private void constructNamespaceDecl() {
        StringBuilder buf = new StringBuilder();
        if (explicitDefaultNSPrefix) {
            buf.append(" xmlns=\"");
            buf.append(defaultNS);
            buf.append("\"");
        }
        for (Iterator it = namespacesToPrefixLookup.keySet().iterator(); it.hasNext();) {
            String nsURI = (String) it.next();
            if (nsURI == null || nsURI.length() == 0) {
                continue;
            }
            String prefix = namespacesToPrefixLookup.get(nsURI);
            if (prefix == null) {
                // Okay, let's auto-assign a prefix.
                // Should we do this??? (REVISIT)
                for (int i=0;i<26;i++) {
                    char[] cc = new char[1];
                    cc[0] = (char) ('a' + i);
                    prefix = new String(cc);
                    if (env.getNamespaceForPrefix(prefix) == null) {
                        break;
                    }
                    prefix = null;
                }
                if (prefix == null) {
                    throw new RuntimeException("This will almost never happen!");
                }
                namespacesToPrefixLookup.put(nsURI, prefix);
            }
            buf.append(" xmlns");
            if (prefix.length() >0) {
                buf.append(":");
                buf.append(prefix);
            }
            buf.append("=\"");
            buf.append(nsURI);
            buf.append("\"");
        }
        this.namespaceDecl = buf.toString();
    }
    
    private void outputQualifiedName(Node n, StringBuilder buf) {
        String nsURI = n.getNamespaceURI();
        if (nsURI == null || nsURI.length() == 0) {
            buf.append(n.getNodeName());
        } else {
            String prefix = namespacesToPrefixLookup.get(nsURI);
            if (prefix == null) {
                //REVISIT!
                buf.append(n.getNodeName());
            } else {
                if (prefix.length() > 0) {
                    buf.append(prefix);
                    buf.append(':');
                }
                buf.append(n.getLocalName());
            }
        }
    }
    
    void outputContent(Node n, StringBuilder buf) {
        switch(n.getNodeType()) {
            case Node.ATTRIBUTE_NODE: {
                if (((Attr) n).getSpecified()) {
                    buf.append(' ');
                    outputQualifiedName(n, buf);
                    buf.append("=\"").append(XMLEncQAttr(n.getNodeValue())).append('"');
                }
                break;
            }
            case Node.COMMENT_NODE: {
                buf.append("<!--").append(n.getNodeValue()).append("-->");
                break;
            }
            case Node.DOCUMENT_NODE: {
                outputContent(n.getChildNodes(), buf);
                break;
            }
            case Node.DOCUMENT_TYPE_NODE: {
                buf.append("<!DOCTYPE ").append(n.getNodeName());
                DocumentType dt = (DocumentType)n;
                if(dt.getPublicId() != null) {
                    buf.append(" PUBLIC \"").append(dt.getPublicId()).append('"');
                }
                if(dt.getSystemId() != null) {
                    buf.append(" \"").append(dt.getSystemId()).append('"');
                }
                if(dt.getInternalSubset() != null) {
                    buf.append(" [").append(dt.getInternalSubset()).append(']');
                }
                buf.append('>');
                break;
            }
            case Node.ELEMENT_NODE: {
                buf.append('<');
                outputQualifiedName(n, buf);
                if (n == contextNode) {
                    buf.append(namespaceDecl);
                }
                outputContent(n.getAttributes(), buf);
                NodeList children = n.getChildNodes();
                if (children.getLength() == 0) {
                    buf.append(" />");
                } else {
                    buf.append('>');
                    outputContent(n.getChildNodes(), buf);
                    buf.append("</");
                    outputQualifiedName(n, buf);
                    buf.append('>');
                }
                break;
            }
            case Node.ENTITY_NODE: {
                outputContent(n.getChildNodes(), buf);
                break;
            }
            case Node.ENTITY_REFERENCE_NODE: {
                buf.append('&').append(n.getNodeName()).append(';');
                break;
            }
            case Node.PROCESSING_INSTRUCTION_NODE: {
                buf.append("<?").append(n.getNodeName()).append(' ').append(n.getNodeValue()).append("?>");
                break;
            }
            /*            
                        case Node.CDATA_SECTION_NODE: {
                            buf.append("<![CDATA[").append(n.getNodeValue()).append("]]>");
                            break;
                        }*/
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE: {
                buf.append(XMLEncNQG(n.getNodeValue()));
                break;
            }
        }
    }

    void outputContent(NodeList nodes, StringBuilder buf) {
        for(int i = 0; i < nodes.getLength(); ++i) {
            outputContent(nodes.item(i), buf);
        }
    }
    
    void outputContent(NamedNodeMap nodes, StringBuilder buf) {
        for(int i = 0; i < nodes.getLength(); ++i) {
            Node n = nodes.item(i);
            if (n.getNodeType() != Node.ATTRIBUTE_NODE 
                || (!n.getNodeName().startsWith("xmlns:") && !n.getNodeName().equals("xmlns"))) 
            { 
                outputContent(n, buf);
            }
        }
    }
    
    String getOpeningTag(Element element) {
        StringBuilder buf = new StringBuilder();
        buf.append('<');
        outputQualifiedName(element, buf);
        buf.append(namespaceDecl);
        outputContent(element.getAttributes(), buf);
        buf.append('>');
        return buf.toString();
    }
    
    String getClosingTag(Element element) {
        StringBuilder buf = new StringBuilder();
        buf.append("</");
        outputQualifiedName(element, buf);
        buf.append('>');
        return buf.toString();
    }

    /**
     *  XML encoding for attributes valies quoted with <tt>"</tt> (not with <tt>'</tt>!).
     *  Also can be used for HTML attributes that are quoted with <tt>"</tt>.
     *  @see #XMLEnc(String)
     */
    private static String XMLEncQAttr(String s) {
        int ln = s.length();
        for (int i = 0; i < ln; i++) {
            char c = s.charAt(i);
            if (c == '<' || c == '&' || c == '"') {
                StringBuilder b =
                        new StringBuilder(s.substring(0, i));
                switch (c) {
                    case '<': b.append("&lt;"); break;
                    case '&': b.append("&amp;"); break;
                    case '"': b.append("&quot;"); break;
                }
                i++;
                int next = i;
                while (i < ln) {
                    c = s.charAt(i);
                    if (c == '<' || c == '&' || c == '"') {
                        b.append(s.substring(next, i));
                        switch (c) {
                            case '<': b.append("&lt;"); break;
                            case '&': b.append("&amp;"); break;
                            case '"': b.append("&quot;"); break;
                        }
                        next = i + 1;
                    }
                    i++;
                }
                if (next < ln) {
                    b.append(s.substring(next));
                }
                s = b.toString();
                break;
            } // if c ==
        } // for
        return s;
    }


    /**
     *  XML encoding without replacing apostrophes and quotation marks and
     *  greater-thans (except in {@code ]]>}).
     *  @see #XMLEnc(String)
     */
    private static String XMLEncNQG(String s) {
        int ln = s.length();
        for (int i = 0; i < ln; i++) {
            char c = s.charAt(i);
            if (c == '<'
                    || (c == '>' && i > 1
                            && s.charAt(i - 1) == ']'
                            && s.charAt(i - 2) == ']')
                    || c == '&') {
                StringBuffer b =
                        new StringBuffer(s.substring(0, i));
                switch (c) {
                    case '<': b.append("&lt;"); break;
                    case '>': b.append("&gt;"); break;
                    case '&': b.append("&amp;"); break;
                    default: throw new RuntimeException("Bug: unexpected char");
                }
                i++;
                int next = i;
                while (i < ln) {
                    c = s.charAt(i);
                    if (c == '<'
                            || (c == '>' && i > 1
                                    && s.charAt(i - 1) == ']'
                                    && s.charAt(i - 2) == ']')
                            || c == '&') {
                        b.append(s.substring(next, i));
                        switch (c) {
                            case '<': b.append("&lt;"); break;
                            case '>': b.append("&gt;"); break;
                            case '&': b.append("&amp;"); break;
                            default: throw new RuntimeException("Bug: unexpected char");
                        }
                        next = i + 1;
                    }
                    i++;
                }
                if (next < ln) {
                    b.append(s.substring(next));
                }
                s = b.toString();
                break;
            } // if c ==
        } // for
        return s;
    }
}
