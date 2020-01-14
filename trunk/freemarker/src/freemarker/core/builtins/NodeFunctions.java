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

package freemarker.core.builtins;

import java.util.List;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.TemplateNode;
import freemarker.template.*;
import freemarker.template.utility.StringUtil;

/**
 * Implementations of ?children, ?node_name, and other 
 * standard functions that operate on nodes
 */

public abstract class NodeFunctions extends ExpressionEvaluatingBuiltIn {

    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) 
    throws TemplateException {
        if (!(model instanceof TemplateNodeModel)) {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "node");
        }
        return apply(env, (TemplateNodeModel)model);
    }

    public abstract TemplateModel apply(Environment env, TemplateNodeModel node) 
    throws TemplateException;
    
    public static class Parent extends NodeFunctions {
        @Override
        public TemplateModel apply(Environment env, TemplateNodeModel node)
        throws TemplateException {
            return node.getParentNode();
        }
    }

    public static class Children extends NodeFunctions {
        @Override
        public TemplateModel apply(Environment env, TemplateNodeModel node)
        throws TemplateException {
            return node.getChildNodes();
        }
    }

    public static class Root extends NodeFunctions {
        @Override
        public TemplateModel apply(Environment env, TemplateNodeModel node)
        throws TemplateException {
            for(;;) {
                final TemplateNodeModel parent = node.getParentNode();
                if(parent == null) {
                    return node;
                }
                node = parent;
            }
        }
    }
    
    public static class NodeName extends NodeFunctions {
        @Override
        public TemplateModel apply(Environment env, TemplateNodeModel node)
        throws TemplateException {
            return new SimpleScalar(node.getNodeName());
        }
    }

    public static class NodeNamespace extends NodeFunctions {
        @Override
        public TemplateModel apply(Environment env, TemplateNodeModel node)
        throws TemplateException {
            String ns = node.getNodeNamespace();
            return ns == null ? TemplateModel.JAVA_NULL : new SimpleScalar(ns);
        }
    }

    public static class NodeType extends NodeFunctions {
        @Override
        public TemplateModel apply(Environment env, TemplateNodeModel node)
        throws TemplateException {
            String nt = node.getNodeType();
            return nt == null ? TemplateModel.JAVA_NULL : new SimpleScalar(nt);
        }
    }
    

    public static class Ancestors extends NodeFunctions {
        @Override
        public TemplateModel apply(Environment env, TemplateNodeModel node)
        throws TemplateException {
            final AncestorSequence result = new AncestorSequence();
            TemplateNodeModel parent = node.getParentNode();
            while (parent != null) {
                result.add(parent);
                parent = parent.getParentNode();
            }
            return result;
        }
    }

    static class AncestorSequence extends SimpleSequence implements TemplateMethodModel {
        private static final long serialVersionUID = 1L;

        public Object exec(List names) throws TemplateModelException {
            if (names == null || names.isEmpty()) {
                return this;
            }
            AncestorSequence result = new AncestorSequence();
            final Environment env = Environment.getCurrentEnvironment();
            for (int i=0; i<size(); i++) {
                TemplateNodeModel tnm = (TemplateNodeModel) get(i);
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
}
