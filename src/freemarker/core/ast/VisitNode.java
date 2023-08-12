package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.core.TemplateNamespace;
import freemarker.ext.beans.ListModel;
import freemarker.core.parser.ast.ListLiteral;
import freemarker.core.parser.ast.StringLiteral;
import freemarker.template.*;

import static freemarker.ext.beans.ObjectWrapper.*;


/**
 * An instruction to visit an XML node.
 */
public class VisitNode extends TemplateElement {
    
    private Expression targetNode, namespaces;
    
    public VisitNode(Expression targetNode, Expression namespaces) {
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
        Object node = targetNode.evaluate(env);
        assertNonNull(node, targetNode, env);
        if (!(node instanceof TemplateNodeModel)) {
            throw new TemplateException("Expecting an XML node here", env);
        }
        Object nss = namespaces == null ? null : namespaces.evaluate(env);
        if (namespaces instanceof StringLiteral) {
            nss = env.importLib(asString(nss), null);
        }
        else if (namespaces instanceof ListLiteral) {
            nss = ((ListLiteral) namespaces).evaluateStringsToNamespaces(env);
        }
        if (nss != null) {
            if (nss instanceof TemplateNamespace) {
                ListModel ss = new ListModel();
                ss.add(nss);
                nss = ss;
            }
            else if (!(nss instanceof TemplateSequenceModel)) {
                throw new TemplateException("Expecting a sequence of namespaces after 'using'", env);
            }
        }
        env.render((TemplateNodeModel) node, (TemplateSequenceModel) nss);
    }

    public String getDescription() {
        return "visit instruction";
    }
}
