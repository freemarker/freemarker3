
 package freemarker3.xml;

import org.w3c.dom.*;
import freemarker3.core.variables.*;
import freemarker3.template.TemplateSequenceModel;

class DocumentTypeModel extends WrappedDomNode {
    
    public DocumentTypeModel(DocumentType docType) {
        super(docType);
    }
    
    public String getAsString() {
        return ((ProcessingInstruction) node).getData();
    }
    
    public TemplateSequenceModel getChildren() {
        throw new EvaluationException("entering the child nodes of a DTD node is not currently supported");
    }
    
    public Object get(String key) {
        throw new EvaluationException("accessing properties of a DTD is not currently supported");
    }
    
    public String getNodeName() {
        return "@document_type$" + ((DocumentType) node).getNodeName();
    }
    
    public boolean isEmpty() {
        return true;
    }
}