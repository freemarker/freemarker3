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
import java.util.*;

import freemarker.core.Environment;
import freemarker.template.*;

public class Param extends TemplateElement {
	
    private String name;
    private ParameterList params;
    private List<Param> subParams;
    
 	public Param(String name, ParameterList params, TemplateElement block) {
 		this.name = name;
 		this.params = params;
 		this.nestedBlock = block;
 	}
 	
 	public String getName() {
 		return name;
 	}

	@Override
	public void execute(Environment env) throws TemplateException, IOException {
		env.unqualifiedSet(name, new Model());
	}
	
	class Expr extends Expression {
		public Expression _deepClone(String s, Expression e){
			return this;
		}
		
		public boolean isLiteral() {
			return false;
		}
		
		public TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
			return Param.this.new Model();
		}
		
	}
	
	public class Model implements TemplateModel {}
	
	static class MultiParam extends Expression {
		
		List<Param> params = new ArrayList<Param>();
		
		public Expression _deepClone(String s, Expression e) {
			return this;
		}
		
		public boolean isLiteral() {
			return false;
		}
		
		public TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
			SimpleSequence result = new SimpleSequence();
			for (Param param : params) {
				result.add(param.new Model());
			}
			return result;
		}
		
	}

}
