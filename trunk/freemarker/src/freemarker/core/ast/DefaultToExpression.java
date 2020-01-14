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


import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.template.*;

public class DefaultToExpression extends Expression {
	
    private static final TemplateCollectionModel EMPTY_COLLECTION = new SimpleCollection(new java.util.ArrayList(0));
    
	static private class EmptyStringAndSequence 
	  implements TemplateScalarModel, TemplateSequenceModel, TemplateHashModelEx {
		public String getAsString() {
			return "";
		}
		public TemplateModel get(int i) {
			return null;
		}
		public TemplateModel get(String s) {
			return null;
		}
		public int size() {
			return 0;
		}
		public boolean isEmpty() {
			return true;
		}
		public TemplateCollectionModel keys() {
			return EMPTY_COLLECTION;
		}
		public TemplateCollectionModel values() {
			return EMPTY_COLLECTION;
		}
		
	}
	
	static final TemplateModel EMPTY_STRING_AND_SEQUENCE = new EmptyStringAndSequence();
	
	private Expression lhs, rhs;
	
	public DefaultToExpression(Expression lhs, Expression rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		lhs.parent = this;
		if (rhs != null) rhs.parent = this;
	}
	
	public Expression getLeft() {
		return lhs;
	}
	
	public Expression getRight() {
		return rhs;
	}

	TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
		TemplateModel left = null;		
		try {
			left = lhs.getAsTemplateModel(env);
		} catch (InvalidReferenceException ire) {
			if (!(lhs instanceof ParentheticalExpression)) {
				throw ire;
			}
		}
		if (left != null && left != TemplateModel.JAVA_NULL) return left;
		if (rhs == null) return EMPTY_STRING_AND_SEQUENCE;
		return rhs.getAsTemplateModel(env);
	}

	boolean isLiteral() {
		return false;
	}

	Expression _deepClone(String name, Expression subst) {
		if (rhs == null) {
			return new DefaultToExpression(lhs.deepClone(name, subst), null);
		}
		return new DefaultToExpression(lhs.deepClone(name, subst), rhs.deepClone(name, subst));
	}
}
