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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.TemplateNamespace;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateSequenceModel;

public class ListLiteral extends Expression {

    private final ArrayList<Expression> values;

    public ListLiteral(ArrayList<Expression> values) {
        this.values = values;
        values.trimToSize();
    }
    
    public List<Expression> getElements() {
    	return Collections.unmodifiableList(values);
    }
    
    PositionalArgsList getAsArgsList() {
    	PositionalArgsList result = new PositionalArgsList();
    	for (Expression exp: values) {
    		result.addArg(exp);
    	}
    	return result;
    }

    TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
        SimpleSequence list = new SimpleSequence(values.size());
        for (Iterator it = values.iterator(); it.hasNext();) {
            Expression exp = (Expression) it.next();
            TemplateModel tm = exp.getAsTemplateModel(env);
            assertIsDefined(tm, exp, env);
            list.add(tm);
        }
        return list;
    }
    
    boolean isLiteral() {
        if (constantValue != null) {
            return true;
        }
        for (int i = 0; i<values.size(); i++) {
            Expression exp = values.get(i);
            if (!exp.isLiteral()) {
                return false;
            }
        }
        return true;
    }
    
    

    // A hacky routine used by VisitNode and RecurseNode
    
    TemplateSequenceModel evaluateStringsToNamespaces(Environment env) throws TemplateException {
        TemplateSequenceModel val = (TemplateSequenceModel) getAsTemplateModel(env);
        SimpleSequence result = new SimpleSequence(val.size());
        for (int i=0; i<values.size(); i++) {
            if (values.get(i) instanceof StringLiteral) {
                String s = ((StringLiteral) values.get(i)).getAsString();
                try {
                    TemplateNamespace ns = env.importLib(s, null);
                    result.add(ns);
                } 
                catch (IOException ioe) {
                    throw new TemplateException("Could not import library '" + s + "', " + ioe.getMessage(), env); 
                }
            }
            else {
                result.add(val.get(i));
            }
        }
        return result;
    }
    
    Expression _deepClone(String name, Expression subst) {
    	ArrayList<Expression> clonedValues = new ArrayList<Expression>(values.size());
    	for (Expression exp : values) {
    		clonedValues.add(exp.deepClone(name, subst));
    	}
        return new ListLiteral(clonedValues);
    }

}
