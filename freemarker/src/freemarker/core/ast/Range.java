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
import freemarker.template.*;

/**
 * A class that represents a Range between two integers.
 */
public class Range extends Expression {

    private Expression left;
    private Expression right;

    public Range(Expression left, Expression right) {
        this.left = left;
        this.right = right;
        left.parent = this;
        if (right != null) right.parent = this;
    }
    
    public Expression getLeft() {
    	return left;
    }
    
    public Expression getRight() {
    	return right;
    }
    
    boolean hasRhs() {
        return right != null;
    }

    TemplateModel _getAsTemplateModel(Environment env) 
        throws TemplateException
    {
        int min = EvaluationUtil.getNumber(left, env).intValue();
        int max = 0;
        if (right != null) {
            max = EvaluationUtil.getNumber(right, env).intValue();
            return new NumericalRange(min, max);
        }
        return new NumericalRange(min);
    }
    
    boolean isTrue(Environment env) throws TemplateException {
        String msg = "Error " + getStartLocation() + ". " 
                    + "\nExpecting a boolean here."
                    + " Expression " + this + " is a range.";
        throw new NonBooleanException(msg, env);
    }

    boolean isLiteral() {
        boolean rightIsLiteral = right == null || right.isLiteral();
        return constantValue != null || (left.isLiteral() && rightIsLiteral);
    }
    
    Expression _deepClone(String name, Expression subst) {
        return new Range(left.deepClone(name, subst), right.deepClone(name, subst));
    }
}
