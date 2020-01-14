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

package freemarker.core.helpers;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.Expression;
import freemarker.template.TemplateModel;
import java.util.*;

public class DefaultReferenceChecker {
	
	protected DefaultReferenceChecker() {}
	static public final DefaultReferenceChecker instance = new DefaultReferenceChecker();
	
	private Locale locale;
	
    public void assertNonNull(TemplateModel model, Expression exp, Environment env) throws InvalidReferenceException {
        assertIsDefined(model, exp, env);
        if (model == TemplateModel.JAVA_NULL) {
            throw new InvalidReferenceException(
                "Expression " + exp + " is null " +
                exp.getStartLocation() + ".", env);
        }
    }
    
    public void assertIsDefined(TemplateModel model, Expression exp, Environment env) throws InvalidReferenceException {
        if (model == null) {
            throw new InvalidReferenceException(
                "Expression " + exp + " is undefined " +
                exp.getStartLocation() + ".", env);
        }
    }
    
    public void setLocale(Locale locale) {
    	this.locale = locale;
    }
    
    public Locale getLocale() {
    	return this.locale;
    }

}
