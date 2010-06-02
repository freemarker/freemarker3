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

import freemarker.core.Environment;
import freemarker.template.*;

/**
 * An operator for arithmetic operations. Note that the + operator
 * also does string concatenation for backward compatibility.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
public class ArithmeticExpression extends Expression {

    static public final int SUBSTRACTION = 0;
    static public final int MULTIPLICATION = 1;
    static public final int DIVISION = 2;
    static public final int MODULUS = 3;

    private Expression left;
    private Expression right;
    private int operation;

    public ArithmeticExpression(Expression left, Expression right, int operation) {
        this.left = left;
        this.right = right;
        this.operation = operation;
        left.parent = this;
        right.parent = this;
    }
    
    public Expression getLeft() {
    	return left;
    }
    
    public Expression getRight() {
    	return right;
    }
    
    public int getOperation() {
    	return operation;
    }
    
    TemplateModel _getAsTemplateModel(Environment env) throws TemplateException 
    {
        TemplateModel leftModel = left.getAsTemplateModel(env);
        TemplateModel rightModel = right.getAsTemplateModel(env);
        boolean leftIsNumber = (leftModel instanceof TemplateNumberModel);
        boolean rightIsNumber = (rightModel instanceof TemplateNumberModel);
        boolean bothNumbers = leftIsNumber && rightIsNumber;
        if (!bothNumbers) {
            String msg = "Error " + getStartLocation();
            if (!leftIsNumber) {
                msg += "\nExpression " + left + " is not numerical";
            }
            if (!rightIsNumber) {
                msg += "\nExpression " + right + " is not numerical";
            }
            throw new NonNumericalException(msg, env);
        }
        Number first = EvaluationUtil.getNumber(leftModel, left, env);
        Number second = EvaluationUtil.getNumber(rightModel, right, env);
        ArithmeticEngine ae = 
            env != null 
                ? env.getArithmeticEngine()
                : getTemplate().getArithmeticEngine();
        switch (operation) {
            case SUBSTRACTION : 
                return new SimpleNumber(ae.subtract(first, second));
            case MULTIPLICATION :
                return new SimpleNumber(ae.multiply(first, second));
            case DIVISION :
                return new SimpleNumber(ae.divide(first, second));
            case MODULUS :
                return new SimpleNumber(ae.modulus(first, second));
            default:
                throw new TemplateException("unknown operation : " + operation, env);
        }
    }

    boolean isLiteral() {
        return constantValue != null || (left.isLiteral() && right.isLiteral());
    }

    Expression _deepClone(String name, Expression subst) {
    	return new ArithmeticExpression(left.deepClone(name, subst), right.deepClone(name, subst), operation);
    }
}
