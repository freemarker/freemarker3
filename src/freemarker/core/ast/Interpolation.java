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
import freemarker.template.TemplateException;

/**
 * An instruction that outputs the value of an <tt>Expression</tt>.
 */
public class Interpolation extends TemplateElement {

    private Expression expression;
    private Expression escapedExpression; // This will be the same as the expression if we are not within an escape block.

    public Interpolation(Expression expression) {
        this.expression = expression;
        this.escapedExpression = expression;
    }
    
    public void setEscapedExpression(Expression escapedExpression) {
    	this.escapedExpression = escapedExpression;
    }
    
    public Expression getEscapedExpression() {
    	return this.escapedExpression;
    }
    
    public Expression getExpression() {
    	return expression;
    }

    /**
     * Outputs the string value of the enclosed expression.
     */
    public void execute(Environment env) throws TemplateException, IOException {
        env.getOut().write(escapedExpression.getStringValue(env));
    }

    public String getDescription() {
        return this.getSource()  +
        (expression == escapedExpression 
            ? "" 
            : " escaped ${" + escapedExpression.getCanonicalForm() + "}");
    }
}
