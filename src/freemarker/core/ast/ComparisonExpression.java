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

import java.text.Collator;
import java.util.Date;

import freemarker.core.Environment;
import freemarker.template.*;

/**
 * A class that handles comparisons.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 * @author Attila Szegedi
 */

public class ComparisonExpression extends BooleanExpression {

    static public final int EQUALS=1;
    static public final int NOT_EQUALS=2;
    static public final int LESS_THAN=3;
    static public final int GREATER_THAN=4;
    static public final int LESS_THAN_EQUALS=5;
    static public final int GREATER_THAN_EQUALS=6;

    private Expression left;
    private Expression right;
    private int operation;
    private final String opString;

    public ComparisonExpression(Expression left, Expression right, String opString) {
        this.left = left;
        this.right = right;
        left.parent = this;
        right.parent = this;
        opString = opString.intern();
        this.opString = opString;
        if (opString == "==" || opString == "=") {
            operation = EQUALS;
        }
        else if (opString == "!=") {
            operation = NOT_EQUALS;
        }
        else if (opString == "gt" || opString == "\\gt" || opString == ">" || opString == "&gt;") {
            operation = GREATER_THAN;
        }
        else if (opString == "gte" || opString == "\\gte" || opString == ">=" || opString == "&gt;=") {
            operation = GREATER_THAN_EQUALS;
        }
        else if (opString== "lt" || opString == "\\lt" || opString == "<" || opString == "&lt;") {
            operation = LESS_THAN;
        }
        else if (opString == "lte" || opString == "\\lte" || opString == "<=" || opString == "&lt;=") {
            operation = LESS_THAN_EQUALS;
        }
        else {
            throw new RuntimeException("Unknown comparison operator " + opString);
        }
    }
    
    public int getOperation() {
    	return operation;
    }
    
    public Expression getLeft() {
    	return left;
    }
    
    public Expression getRight() {
    	return right;
    }
    

    

    

    /*
     * WARNING! This algorithm is duplicated in SequenceBuiltins.modelsEqual.
     * Thus, if you update this method, then you have to update that too!
     */
    boolean isTrue(Environment env) throws TemplateException {
        TemplateModel ltm = left.getAsTemplateModel(env);
        TemplateModel rtm = right.getAsTemplateModel(env);
/*
  The following block that allows comparison of nulls is now commented out.        
        if (ltm == TemplateModel.JAVA_NULL || rtm == TemplateModel.JAVA_NULL) {
        	if (operation != EQUALS && operation != NOT_EQUALS) {
        		throw new TemplateException("Cannot use operator " + opString + " to compare to null.", env);
        	}
        	if (ltm == null) assertNonNull(ltm, left, env);
        	if (rtm == null) assertNonNull(rtm, right, env);
        	return (operation == EQUALS) ? ltm == rtm : ltm != rtm;
        }
*/        
        assertNonNull(ltm, left, env);
       	assertNonNull(rtm, right, env);
        int comp = 0;
        if(ltm instanceof TemplateNumberModel && rtm instanceof TemplateNumberModel) { 
            Number first = EvaluationUtil.getNumber((TemplateNumberModel)ltm, left, env);
            Number second = EvaluationUtil.getNumber((TemplateNumberModel)rtm, right, env);
            ArithmeticEngine ae = 
                env != null 
                    ? env.getArithmeticEngine()
                    : getTemplate().getArithmeticEngine();
            comp = ae.compareNumbers(first, second);
        }
        else if(ltm instanceof TemplateDateModel && rtm instanceof TemplateDateModel) {
            TemplateDateModel ltdm = (TemplateDateModel)ltm;
            TemplateDateModel rtdm = (TemplateDateModel)rtm;
            int ltype = ltdm.getDateType();
            int rtype = rtdm.getDateType();
            if(ltype != rtype) {
                throw new TemplateException(
                    "Can not compare dates of different type. Left date is of "
                    + TemplateDateModel.TYPE_NAMES.get(ltype)
                    + " type, right date is of " 
                    + TemplateDateModel.TYPE_NAMES.get(rtype) + " type.", 
                    env);
            }
            if(ltype == TemplateDateModel.UNKNOWN) {
                throw new TemplateException(
                    "Left date is of UNKNOWN type, and can not be compared.", env);
            }
            if(rtype == TemplateDateModel.UNKNOWN) {
                throw new TemplateException(
                    "Right date is of UNKNOWN type, and can not be compared.", env);
            }
            
            Date first = EvaluationUtil.getDate(ltdm, left, env);
            Date second = EvaluationUtil.getDate(rtdm, right, env);
            comp = first.compareTo(second);
        }
        else if(ltm instanceof TemplateScalarModel && rtm instanceof TemplateScalarModel) {
            if(operation != EQUALS && operation != NOT_EQUALS) {
                throw new TemplateException("Can not use operator " + opString + " on string values.", env);
            }
            String first = EvaluationUtil.getString((TemplateScalarModel)ltm, left, env);
            String second = EvaluationUtil.getString((TemplateScalarModel)rtm, right, env);
            Collator collator;
            if(env == null) {
                collator = Collator.getInstance(getTemplate().getLocale());
            } else {
                collator = env.getCollator();
            }
            comp = collator.compare(first, second);
        }
        else if(ltm instanceof TemplateBooleanModel && rtm instanceof TemplateBooleanModel) {
            if(operation != EQUALS && operation != NOT_EQUALS) {
                throw new TemplateException("Can not use operator " + opString + " on boolean values.", env);
            }
            boolean first = ((TemplateBooleanModel)ltm).getAsBoolean();
            boolean second = ((TemplateBooleanModel)rtm).getAsBoolean();
            comp = (first ? 1 : 0) - (second ? 1 : 0);
        }
        else {
            throw new TemplateException(
                  "The only legal comparisons are between two numbers, two strings, or two dates.\n"
                + "Left  hand operand is a " + ltm.getClass().getName() + "\n"
                + "Right hand operand is a " + rtm.getClass().getName() + "\n"
                , env);
        }
        switch (operation) {
            case EQUALS:
                return comp == 0;
            case NOT_EQUALS:
                return comp != 0;
            case LESS_THAN : 
                return comp < 0;
            case GREATER_THAN : 
                return comp > 0;
            case LESS_THAN_EQUALS :
                return comp <= 0;
            case GREATER_THAN_EQUALS :
                return comp >= 0;
            default :
                throw new TemplateException("unknown operation", env);
        }
    }

    boolean isLiteral() {
        return constantValue != null || (left.isLiteral() && right.isLiteral());
    }

    Expression _deepClone(String name, Expression subst) {
    	return new ComparisonExpression(left.deepClone(name, subst), right.deepClone(name, subst), opString);
    }
}
