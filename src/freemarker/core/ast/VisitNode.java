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

package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.core.TemplateNamespace;
import freemarker.template.*;


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
        TemplateModel node = targetNode.getAsTemplateModel(env);
        assertNonNull(node, targetNode, env);
        if (!(node instanceof TemplateNodeModel)) {
            throw new TemplateException("Expecting an XML node here", env);
        }
        TemplateModel nss = namespaces == null ? null : namespaces.getAsTemplateModel(env);
        if (namespaces instanceof StringLiteral) {
            nss = env.importLib(((TemplateScalarModel) nss).getAsString(), null);
        }
        else if (namespaces instanceof ListLiteral) {
            nss = ((ListLiteral) namespaces).evaluateStringsToNamespaces(env);
        }
        if (nss != null) {
            if (nss instanceof TemplateNamespace) {
                SimpleSequence ss = new SimpleSequence(1);
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
