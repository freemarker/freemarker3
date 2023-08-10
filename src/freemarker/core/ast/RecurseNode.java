package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.ext.beans.ListModel;
import freemarker.core.parser.ast.ListLiteral;
import freemarker.core.parser.ast.StringLiteral;
import freemarker.template.*;


/**
 * An instruction to visit the children of a node.
 */
public class RecurseNode extends TemplateElement {
    
    private Expression targetNode, namespaces;
    
    public RecurseNode(Expression targetNode, Expression namespaces) {
        this.targetNode = targetNode;
        this.namespaces = namespaces;
    }
    
    public Expression getTargetNode() {
    	return targetNode;
    }
    
    public Expression getNamespaces() {
    	return namespaces;
    }

    public void execute(Environment env) throws IOException, TemplateException {
        Object node = targetNode == null ? null : targetNode.getAsTemplateModel(env);
        Object nss = namespaces == null ? null : namespaces.getAsTemplateModel(env);
        if (namespaces instanceof StringLiteral) {
            nss = env.importLib(((TemplateScalarModel) nss).getAsString(), null);
        }
        else if (namespaces instanceof ListLiteral) {
            nss = ((ListLiteral) namespaces).evaluateStringsToNamespaces(env);
        }
        if (node != null && !(node instanceof TemplateNodeModel)) {
            throw new TemplateException("Expecting an XML node here, for expression: " + targetNode + ", found a: " + node.getClass().getName(), env);
        }
        if (nss != null) {
            if (nss instanceof TemplateHashModel) {
                ListModel ss = new ListModel();
                ss.add(nss);
                nss = ss;
            }
            else if (!(nss instanceof TemplateSequenceModel)) {
                throw new TemplateException("Expecting a sequence of namespaces after 'using'", env);
            }
        }
        
        env.process((TemplateNodeModel) node, (TemplateSequenceModel) nss);
    }

    public String getDescription() {
        return "recurse instruction";
    }
}
