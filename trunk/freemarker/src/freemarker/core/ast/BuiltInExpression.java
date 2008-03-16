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

package freemarker.core.ast;

import java.util.HashMap;
import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.builtins.*;



public class BuiltInExpression extends Expression implements Cloneable {
	
	static final HashMap<String, BuiltIn> knownBuiltins = new HashMap<String, BuiltIn>();
	
	{
		knownBuiltins.put("c", new cBI());
		knownBuiltins.put("size", new sizeBI());
		knownBuiltins.put("string", new stringBI());
		knownBuiltins.put("eval", new evalBI());
		knownBuiltins.put("new", new newBI());
		knownBuiltins.put("interpret", new interpretBI());
		knownBuiltins.put("resolve", new resolveBI());
		knownBuiltins.put("use_defaults", new Curry());
		NumericalCast numericalCast = new NumericalCast();
		knownBuiltins.put("byte", numericalCast);
		knownBuiltins.put("double", numericalCast);
		knownBuiltins.put("float", numericalCast);
		knownBuiltins.put("int", numericalCast);
		knownBuiltins.put("long", numericalCast);
		knownBuiltins.put("short", numericalCast);
		StringTransformations stringTransformations = new StringTransformations();
		knownBuiltins.put("capitalize", stringTransformations);
		knownBuiltins.put("lower_case", stringTransformations);
		knownBuiltins.put("upper_case", stringTransformations);
		knownBuiltins.put("cap_first", stringTransformations);
		knownBuiltins.put("uncap_first", stringTransformations);
		knownBuiltins.put("j_string", stringTransformations);
		knownBuiltins.put("js_string", stringTransformations);
		knownBuiltins.put("chop_linebreak", stringTransformations);
		knownBuiltins.put("trim", stringTransformations);
		knownBuiltins.put("html", stringTransformations);
		knownBuiltins.put("rtf", stringTransformations);
		knownBuiltins.put("xml", stringTransformations);
		knownBuiltins.put("web_safe", stringTransformations);
		TypeChecks typeChecks = new TypeChecks();
		knownBuiltins.put("is_boolean", typeChecks);
		knownBuiltins.put("is_collection", typeChecks);
		knownBuiltins.put("is_date", typeChecks);
		knownBuiltins.put("is_enumerable", typeChecks);
		knownBuiltins.put("is_hash", typeChecks);
		knownBuiltins.put("is_hash_ex", typeChecks);
		knownBuiltins.put("is_indexable", typeChecks);
		knownBuiltins.put("is_directive", typeChecks);
		knownBuiltins.put("is_method", typeChecks);
		knownBuiltins.put("is_null", typeChecks);
		knownBuiltins.put("is_number", typeChecks);
		knownBuiltins.put("is_macro", typeChecks);
		knownBuiltins.put("is_node", typeChecks);
		knownBuiltins.put("is_sequence", typeChecks);
		knownBuiltins.put("is_string", typeChecks);
		knownBuiltins.put("is_transform", typeChecks);
		StringFunctions stringFunctions = new StringFunctions();
		knownBuiltins.put("index_of", stringFunctions);
		knownBuiltins.put("last_index_of", stringFunctions);
		knownBuiltins.put("contains", stringFunctions);
		knownBuiltins.put("number", stringFunctions);
		knownBuiltins.put("left_pad", stringFunctions);
		knownBuiltins.put("right_pad", stringFunctions);
		knownBuiltins.put("length", stringFunctions);
		knownBuiltins.put("replace", stringFunctions);
		knownBuiltins.put("split", stringFunctions);
		knownBuiltins.put("groups", stringFunctions);
		knownBuiltins.put("matches", stringFunctions);
		knownBuiltins.put("starts_with", stringFunctions);
		knownBuiltins.put("ends_with", stringFunctions);
		knownBuiltins.put("substring", stringFunctions);
		knownBuiltins.put("word_list", stringFunctions);
		knownBuiltins.put("url", stringFunctions);
		NodeFunctions nodeFunctions = new NodeFunctions();
		knownBuiltins.put("parent", nodeFunctions);
		knownBuiltins.put("children", nodeFunctions);
		knownBuiltins.put("node_name", nodeFunctions);
		knownBuiltins.put("node_type", nodeFunctions);
		knownBuiltins.put("node_namespace", nodeFunctions);
		knownBuiltins.put("root", nodeFunctions);
		knownBuiltins.put("ancestors", nodeFunctions);
		SequenceFunctions sequenceFunctions = new SequenceFunctions();
		knownBuiltins.put("first", sequenceFunctions);
		knownBuiltins.put("last", sequenceFunctions);
		knownBuiltins.put("reverse", sequenceFunctions);
		knownBuiltins.put("sort", sequenceFunctions);
		knownBuiltins.put("sort_by", sequenceFunctions);
		knownBuiltins.put("chunk", sequenceFunctions);
		knownBuiltins.put("seq_contains", sequenceFunctions);
		knownBuiltins.put("seq_index_of", sequenceFunctions);
		knownBuiltins.put("seq_last_index_of", sequenceFunctions);
        MacroBuiltins macroBuiltins = new MacroBuiltins();
        knownBuiltins.put("scope", macroBuiltins);
        knownBuiltins.put("namespace", macroBuiltins);
        HashBuiltins hashBuiltins = new HashBuiltins();
        knownBuiltins.put("keys", hashBuiltins);
        knownBuiltins.put("values", hashBuiltins);
        DateTime dateTime = new DateTime();
        knownBuiltins.put("date", dateTime);
        knownBuiltins.put("time", dateTime);
        knownBuiltins.put("datetime", dateTime);
        ExistenceBIs existenceBIs = new ExistenceBIs();
        knownBuiltins.put("is_defined", existenceBIs);
        knownBuiltins.put("if_exists", existenceBIs);
        knownBuiltins.put("exists", existenceBIs);
        knownBuiltins.put("default", existenceBIs);
        knownBuiltins.put("has_content", existenceBIs);
        knownBuiltins.put("source", new sourceBI());
	}
	
	Expression target;
	String key;
	
	BuiltIn bi;
	
	boolean protectTargetEvaluation, isExistenceBI;
	
	public BuiltInExpression(Expression target, String key) {
		this.target = target;
		target.parent = this;
		this.key = key.intern();
		this.isExistenceBI = this.key == "exists" 
			              || this.key == "if_exists" 
			              || this.key == "default" 
			              || this.key == "has_content"
			              || this.key == "is_defined";
		this.protectTargetEvaluation = isExistenceBI && target instanceof ParentheticalExpression;
	}
	
	public BuiltIn findImplementation() {
		return knownBuiltins.get(key);
	}
	
	TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
		if (bi == null) bi = knownBuiltins.get(key); // WHy is this necessary? (REVISIT)
		TemplateModel targetModel = null;
		if (key != "source") try { // If this is the source built-in, we don't try to evaluate.
			targetModel = target.getAsTemplateModel(env);
		} catch (InvalidReferenceException ire) {
			if (!protectTargetEvaluation) throw ire;
		}
		return bi.get(targetModel, key, env, this);
	}
	
	public Expression getTarget() {
		return target;
	}
	
	public String getName() {
		return key;
	}

	boolean isLiteral() {
		return false;
	}
	
/*	
	@Override
    Expression _deepClone(String name, Expression subst) {
		findImplementation();
    	try {
	    	BuiltInExpression clone = (BuiltInExpression) this.clone();
	    	clone.target = target.deepClone(name, subst);
	    	clone.bi = knownBuiltins.get(key);
	    	return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }*/
	
	Expression _deepClone(String name, Expression subst) {
		return new BuiltInExpression(target.deepClone(name, subst), key);
	}
	
}
