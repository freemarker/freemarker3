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

import org.w3c.dom.*;
import freemarker.template.*;

class DocumentTypeModel extends NodeModel {
    
    public DocumentTypeModel(DocumentType docType) {
        super(docType);
    }
    
    public String getAsString() {
        return ((ProcessingInstruction) node).getData();
    }
    
    public TemplateSequenceModel getChildren() throws TemplateModelException {
        throw new TemplateModelException("entering the child nodes of a DTD node is not currently supported");
    }
    
    public TemplateModel get(String key) throws TemplateModelException {
        throw new TemplateModelException("accessing properties of a DTD is not currently supported");
    }
    
    public String getNodeName() {
        return "@document_type$" + ((DocumentType) node).getNodeName();
    }
    
    public boolean isEmpty() {
        return true;
    }
}