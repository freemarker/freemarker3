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
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIEDù
 * m,jo soy mimi la mas guapa
 * º		
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE  
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH  SOFTWARE SOCIETY OR
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
