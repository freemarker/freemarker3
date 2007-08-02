/*
 * Copyright (c) 2007 The Visigoth Software Society. All rights
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

package freemarker.core.builtins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.template.*;
import freemarker.template.utility.StringUtil;

/**
 * Implementations of ?children, ?node_name, and other 
 * standard functions that operate on nodes
 */

public class NodeFunctions extends BuiltIn {
	

	public TemplateModel get(TemplateModel target, String builtInName, Environment env, BuiltInExpression callingExpression) throws TemplateException {
		if (!(target instanceof TemplateNodeModel)) {
			throw callingExpression.invalidTypeException(target, callingExpression.getTarget(), env, "node");
		}
		TemplateNodeModel node = (TemplateNodeModel) target;
		return getNodeFunction(node, builtInName, env, callingExpression);
	}
	
	private TemplateModel getNodeFunction(TemplateNodeModel node, String builtInName, 
			Environment env, BuiltInExpression callingExpression) throws TemplateException 
	{
		if (builtInName == "parent") {
			return node.getParentNode();
		}
		if (builtInName == "children") {
			return node.getChildNodes();
		}
		if (builtInName == "root") {
			TemplateNodeModel result = node;
			while (result.getParentNode() != null) {
				result = result.getParentNode();
			}
			return result;
		}
		if (builtInName == "node_name") {
			return new SimpleScalar(node.getNodeName());
		}
		if (builtInName == "node_namespace") {
			String ns = node.getNodeNamespace();
			return ns == null ? TemplateModel.JAVA_NULL : new SimpleScalar(ns);
		}
		if (builtInName == "node_type") {
			String nt = node.getNodeType();
			return nt == null ? TemplateModel.JAVA_NULL : new SimpleScalar(nt);
		}
		if (builtInName == "ancestors") {
           AncestorSequence result = new AncestorSequence(env);
           TemplateNodeModel parent = node.getParentNode();
           while (parent != null) {
               result.add(parent);
               parent = parent.getParentNode();
           }
           return result;
		}
		throw new InternalError("Cannot deal with built-in ?" + builtInName);
	}
	
    static class AncestorSequence extends SimpleSequence implements TemplateMethodModel {

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
