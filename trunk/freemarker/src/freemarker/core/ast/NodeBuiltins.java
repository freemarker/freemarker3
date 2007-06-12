/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.template.utility.StringUtil;
import java.util.List;

/**
 * A holder for builtins that operate on TemplateNodeModels.
 */

abstract class NodeBuiltins {
    
    abstract static class NodeBuiltIn extends BuiltIn {
        TemplateModel _getAsTemplateModel(Environment env)
                throws TemplateException
        {
            TemplateModel model = target.getAsTemplateModel(env);
            if (!(model instanceof TemplateNodeModel)) {
                throw invalidTypeException(model, target, env, "node model");
            }
            return calculateResult((TemplateNodeModel) model, env);
        }
        abstract TemplateModel calculateResult(TemplateNodeModel nodeModel, Environment env)
                throws TemplateModelException;
    }

    static class ancestorsBI extends NodeBuiltIn {
       TemplateModel calculateResult(TemplateNodeModel nodeModel, Environment env) throws TemplateModelException {
           AncestorSequence result = new AncestorSequence(env);
           TemplateNodeModel parent = nodeModel.getParentNode();
           while (parent != null) {
               result.add(parent);
               parent = parent.getParentNode();
           }
           return result;
       }
    }
    
    static class childrenBI extends NodeBuiltIn {
       TemplateModel calculateResult(TemplateNodeModel nodeModel, Environment env) throws TemplateModelException {
            return nodeModel.getChildNodes();
       }
    }
    
    
    static class node_nameBI extends NodeBuiltIn {
       TemplateModel calculateResult(TemplateNodeModel nodeModel, Environment env) throws TemplateModelException {
            return new SimpleScalar(nodeModel.getNodeName());
       }
    }
    
    static class node_typeBI extends NodeBuiltIn {
       TemplateModel calculateResult(TemplateNodeModel nodeModel, Environment env) throws TemplateModelException {
            return new SimpleScalar(nodeModel.getNodeType());
        }
    }

    static class parentBI extends NodeBuiltIn {
       TemplateModel calculateResult(TemplateNodeModel nodeModel, Environment env) throws TemplateModelException {
            return nodeModel.getParentNode();
       }
    }
    
    static class rootBI extends NodeBuiltIn {
       TemplateModel calculateResult(TemplateNodeModel nodeModel, Environment env) throws TemplateModelException {
            TemplateNodeModel result = nodeModel;
            TemplateNodeModel parent = nodeModel.getParentNode();
            while (parent != null) {
                result = parent;
                parent = result.getParentNode();
            }
            return result;
       }
    }
    
    static class node_namespaceBI extends NodeBuiltIn {
        TemplateModel calculateResult(TemplateNodeModel nodeModel, Environment env) throws TemplateModelException {
            String nsURI = nodeModel.getNodeNamespace();
            return nsURI == null ? null : new SimpleScalar(nsURI);
        }
    }
    
    
    static class AncestorSequence extends SimpleSequence implements TemplateMethodModel {
        private static final long serialVersionUID = -3486958880179062533L;

        private Environment env;
        
        AncestorSequence(Environment env) {
            this.env = env;
        }
        
        public Object exec(List names) throws TemplateModelException {
            if (names == null || names.isEmpty()) {
                return this;
            }
            AncestorSequence result = new AncestorSequence(env);
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