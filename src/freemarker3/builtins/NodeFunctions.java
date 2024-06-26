package freemarker3.builtins;

import java.util.ArrayList;
import java.util.List;

import freemarker3.core.Environment;
import freemarker3.core.nodes.generated.BuiltInExpression;
import freemarker3.core.nodes.generated.TemplateNode;
import freemarker3.core.variables.WrappedNode;
import static freemarker3.core.variables.Wrap.JAVA_NULL;

/**
 * Implementations of ?children, ?node_name, and other 
 * standard functions that operate on nodes
 */

public abstract class NodeFunctions extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller, Object model) 
    {
        if (!(model instanceof WrappedNode)) {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "node");
        }
        return apply(env, (WrappedNode)model);
    }

    public abstract Object apply(Environment env, WrappedNode node);
    
    public static class Parent extends NodeFunctions {
        @Override
        public Object apply(Environment env, WrappedNode node)
        {
            return node.getParentNode();
        }
    }

    public static class Children extends NodeFunctions {
        @Override
        public Object apply(Environment env, WrappedNode node)
        {
            return node.getChildNodes();
        }
    }

    public static class Root extends NodeFunctions {
        @Override
        public Object apply(Environment env, WrappedNode node)
        {
            for(;;) {
                final WrappedNode parent = node.getParentNode();
                if(parent == null) {
                    return node;
                }
                node = parent;
            }
        }
    }
    
    public static class NodeName extends NodeFunctions {
        @Override
        public Object apply(Environment env, WrappedNode node)
        {
            return node.getNodeName();
        }
    }

    public static class NodeNamespace extends NodeFunctions {
        @Override
        public Object apply(Environment env, WrappedNode node)
        {
            String ns = node.getNodeNamespace();
            return ns == null ? JAVA_NULL : ns;
        }
    }

    public static class NodeType extends NodeFunctions {
        @Override
        public Object apply(Environment env, WrappedNode node)
        {
            String nt = node.getNodeType();
            return nt == null ? JAVA_NULL : nt;
        }
    }
    

    public static class Ancestors extends NodeFunctions {
        @Override
        public Object apply(Environment env, WrappedNode node)
        {
            List<WrappedNode> result = new ArrayList<>();
            WrappedNode parent = node.getParentNode();
            while (parent != null) {
                result.add(parent);
                parent = parent.getParentNode();
            }
            return result;
        }
    }
/*
    static class AncestorSequence extends ListModel implements WrappedMethod {
        public Object exec(List names) {
            if (names == null || names.isEmpty()) {
                return this;
            }
            AncestorSequence result = new AncestorSequence();
            final Environment env = Environment.getCurrentEnvironment();
            for (int i=0; i<size(); i++) {
                WrappedNode tnm = (WrappedNode) get(i);
                String nodeName = tnm.getNodeName();
                String nsURI = tnm.getNodeNamespace();
                if (nsURI == null) {
                    if (names.contains(nodeName)) {
                        result.add(tnm);
                    }
                } else {
                    for (int j = 0; j<names.size(); j++) {
                        if (StringUtil.matchesName((String) names.get(j), nodeName, nsURI, env)) {
                            result.add(tnm);
                            break;
                        }
                    }
                }
            }
            return result;
        }
    }	
*/    
}
