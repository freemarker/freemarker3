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
import freemarker.core.Environment;

import org.w3c.dom.*;

class AttributeNodeModel extends NodeModel implements TemplateScalarModel {
    
    public AttributeNodeModel(Attr att) {
        super(att);
    }
    
    public String getAsString() {
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