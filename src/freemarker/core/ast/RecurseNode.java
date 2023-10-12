package freemarker.core.ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.Scope;
import freemarker.core.parser.ast.Expression;
import freemarker.core.parser.ast.ListLiteral;
import freemarker.core.parser.ast.StringLiteral;
import freemarker.core.parser.ast.TemplateElement;
import freemarker.template.*;

import static freemarker.ext.beans.ObjectWrapper.*;


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
        Object node = targetNode == null ? null : targetNode.evaluate(env);
        Object nss = namespaces == null ? null : namespaces.evaluate(env);
        if (namespaces instanceof StringLiteral) {
                nss = env.importLib(asString(nss), null);
        }
        else if (namespaces instanceof ListLiteral) {
            nss = ((ListLiteral) namespaces).evaluateStringsToNamespaces(env);
        }
        if (node != null && !(node instanceof TemplateNodeModel)) {
            throw new TemplateException("Expecting an XML node here, for expression: " + targetNode + ", found a: " + node.getClass().getName(), env);
        }
        if (nss != null) {
            if (nss instanceof TemplateHashModel) {
                List<Scope> ss = new ArrayList<>();
                ss.add((Scope)nss);
                nss = ss;
            }
            else if (!isList(nss)) {
                throw new TemplateException("Expecting a sequence of namespaces after 'using'", env);
            }
        }
        env.process((TemplateNodeModel) node, (List<Scope>)nss);
    }

    public String getDescription() {
        return "recurse instruction";
    }
}
