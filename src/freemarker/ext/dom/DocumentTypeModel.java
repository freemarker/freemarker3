
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