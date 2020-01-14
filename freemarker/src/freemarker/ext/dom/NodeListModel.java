/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
package freemarker.ext.dom;

import freemarker.template.*;
import freemarker.template.utility.StringUtil;
import freemarker.core.Environment;

import org.w3c.dom.*;
import java.util.List; 
import java.util.ArrayList;

class NodeListModel extends SimpleSequence implements TemplateHashModel, TemplateBooleanModel {
    private static final long serialVersionUID = -7992619396981774837L;

    NodeModel contextNode;
    XPathSupport xpathSupport;
    
    private static ObjectWrapper nodeWrapper = new ObjectWrapper() {
        public TemplateModel wrap(Object obj) {
            if (obj instanceof NodeModel) {
                return (NodeModel) obj;
            }
            return NodeModel.wrap((Node) obj);
        }
    };
    
    
    NodeListModel(Node node) {
        this(NodeModel.wrap(node));
    }
    
    NodeListModel(NodeModel contextNode) {
        super(nodeWrapper);
        this.contextNode = contextNode;
    }
    
    NodeListModel(NodeList nodeList, NodeModel contextNode) {
        super(nodeWrapper);
        for (int i=0; i<nodeList.getLength();i++) {
            list.add(nodeList.item(i));
        }
        this.contextNode = contextNode;
    }
    
    NodeListModel(NamedNodeMap nodeList, NodeModel contextNode) {
        super(nodeWrapper);
        for (int i=0; i<nodeList.getLength();i++) {
            list.add(nodeList.item(i));
        }
        this.contextNode = contextNode;
    }
    
    NodeListModel(List list, NodeModel contextNode) {
        super(list, nodeWrapper);
        this.contextNode = contextNode;
    }
    
    NodeListModel filterByName(String name) throws TemplateModelException {
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
    
    public TemplateModel get(String key) throws TemplateModelException {
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
                TemplateScalarModel textModel = (TemplateScalarModel) nm.get(key);
                result.append(textModel.getAsString());
            }
            return new SimpleScalar(result.toString());
        }
        if (StringUtil.isXMLID(key) 
            || ((key.startsWith("@") && StringUtil.isXMLID(key.substring(1))))
            || key.equals("*") || key.equals("**") || key.equals("@@") || key.equals("@*")) 
        {
            NodeListModel result = new NodeListModel(contextNode);
            for (int i=0; i<size(); i++) {
                NodeModel nm = (NodeModel) get(i);
                if (nm instanceof ElementModel) {
                    TemplateSequenceModel tsm = (TemplateSequenceModel) ((ElementModel) nm).get(key);
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
        throw new TemplateModelException("Key: '" + key + "' is not legal for a node sequence ("
                + this.getClass().getName() + "). This node sequence contains " + size() + " node(s). "
                + "Some keys are valid only for node sequences of size 1. "
                + "If you use Xalan (instead of Jaxen), XPath expression keys work only with "
                + "node lists of size 1.");
    }
    
    private List rawNodeList() throws TemplateModelException {
        int size = size();
        ArrayList al = new ArrayList(size);
        for (int i=0; i<size; i++) {
            al.add(((NodeModel) get(i)).node);
        }
        return al;
    }
    
    XPathSupport getXPathSupport() throws TemplateModelException {
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
    
    public boolean getAsBoolean() {
    	return size() != 0;
    }
}