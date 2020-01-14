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

public class UnaryPlusMinusExpression extends Expression {

    private Expression target;
    private boolean isMinus;
    private static final Integer MINUS_ONE = Integer.valueOf(-1); 

    public UnaryPlusMinusExpression(Expression target, boolean isMinus) {
        this.target = target;
        this.isMinus = isMinus;
        target.parent = this;
    }
    
    public Expression getTarget() {
    	return target;
    }
    
    public boolean isMinus() {
    	return isMinus;
    }
    
    TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
        TemplateNumberModel targetModel = null;
        try {
            targetModel = (TemplateNumberModel) target.getAsTemplateModel(env);
        } catch (ClassCastException cce) {
            String msg = "Error " + getStartLocation();
            msg += "\nExpression " + target + " is not numerical.";
            throw new NonNumericalException(msg, env);
        }
        if (!isMinus) {
            return targetModel;
        }
        Number n = targetModel.getAsNumber();
        n = ArithmeticEngine.CONSERVATIVE_ENGINE.multiply(MINUS_ONE, n);
        return new SimpleNumber(n);
    }

    boolean isLiteral() {
        return target.isLiteral();
    }

    Expression _deepClone(String name, Expression subst) {
    	return new UnaryPlusMinusExpression(target.deepClone(name, subst), isMinus);
    }
}
