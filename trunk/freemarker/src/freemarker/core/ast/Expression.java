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

import freemarker.template.*;
import freemarker.ext.beans.BeanModel;
import freemarker.core.Environment;
import freemarker.core.parser.ParseException;


/**
 * An abstract class for nodes in the parse tree 
 * that represent a FreeMarker expression.
 */
abstract public class Expression extends TemplateNode {

    abstract TemplateModel _getAsTemplateModel(Environment env) throws TemplateException;
    abstract boolean isLiteral();
    
    public String getDescription() {
    	return "the expression: "  + this;
    }

    // Used to store a constant return value for this expression. Only if it
    // is possible, of course.
    
    TemplateModel constantValue;
    
    /**
     * @return the value of the expression if it is a literal, null otherwise.
     */
    
    public final TemplateModel literalValue() {
    	return constantValue;
    }
    
    // Hook in here to set the constant value if possible.
    
    public void setLocation(Template template, int beginColumn, int beginLine, int endColumn, int endLine)
    throws
        ParseException
    {
        super.setLocation(template, beginColumn, beginLine, endColumn, endLine);
        if (isLiteral()) {
        	try {
        		constantValue = _getAsTemplateModel(null);
        	} catch (Exception e) {
        		constantValue = TemplateModel.INVALID_EXPRESSION; // If we can't evaluate it, it must be invalid, no?
        	}
        }
    }
    
    public final TemplateModel getAsTemplateModel(Environment env) throws TemplateException {
        return constantValue != null ? constantValue : _getAsTemplateModel(env);
    }
    
    String getStringValue(Environment env) throws TemplateException {
        return getStringValue(getAsTemplateModel(env), this, env);
    }
    
    static boolean isDisplayableAsString(TemplateModel tm) {
    	return tm instanceof TemplateScalarModel
    	     ||tm instanceof TemplateNumberModel
    	     || tm instanceof TemplateDateModel;
    }
    
    static String getStringValue(TemplateModel referentModel, Expression exp, Environment env)
    throws
        TemplateException
    {
        if (referentModel instanceof TemplateNumberModel) {
            return env.formatNumber(EvaluationUtil.getNumber((TemplateNumberModel) referentModel, exp, env));
        }
        if (referentModel instanceof TemplateDateModel) {
            TemplateDateModel dm = (TemplateDateModel) referentModel;
            return env.formatDate(EvaluationUtil.getDate(dm, exp, env), dm.getDateType());
        }
        if (referentModel instanceof TemplateScalarModel) {
            return EvaluationUtil.getString((TemplateScalarModel) referentModel, exp, env);
        }
        if(env.isClassicCompatible()) {
            if (referentModel instanceof TemplateBooleanModel) {
                return ((TemplateBooleanModel)referentModel).getAsBoolean() ? "true" : "";
            }
            if (referentModel == null || referentModel == TemplateModel.JAVA_NULL) {
                return "";
            }
        }
        assertNonNull(referentModel, exp, env);
        
        String msg = "Error " + exp.getStartLocation()
                     +"\nExpecting a string, " 
                     + (env.isClassicCompatible() ? "boolean, " : "" )
                     + "date or number here, Expression " + exp 
                     + " is instead a " 
                     + referentModel.getClass().getName();
        throw new NonStringException(msg, env);
    }

    Expression deepClone(String name, Expression subst) {
        Expression clone = _deepClone(name, subst);
        clone.copyLocationFrom(this);
        return clone;
    }

    abstract Expression _deepClone(String name, Expression subst);

    boolean isTrue(Environment env) throws TemplateException {
        TemplateModel referent = getAsTemplateModel(env);
        if (referent instanceof TemplateBooleanModel) {
            return ((TemplateBooleanModel) referent).getAsBoolean();
        }
        if (env.isClassicCompatible()) {
            return referent != null && !isEmpty(referent);
        }
        assertNonNull(referent, this, env);
        String msg = "Error " + getStartLocation()
                     + "\nExpecting a boolean (true/false) expression here"
                     + "\nExpression " + this + " does not evaluate to true/false "
                     + "\nit is an instance of " + referent.getClass().getName();
        throw new NonBooleanException(msg, env);
    }


    static boolean isEmpty(TemplateModel model) throws TemplateModelException
    {
        if (model instanceof BeanModel) {
            return ((BeanModel) model).isEmpty();
        } else if (model instanceof TemplateSequenceModel) {
            return ((TemplateSequenceModel) model).size() == 0;
        } else if (model instanceof TemplateScalarModel) {
            String s = ((TemplateScalarModel) model).getAsString();
            return (s == null || s.length() == 0);
        } else if (model instanceof TemplateCollectionModel) {
            return !((TemplateCollectionModel) model).iterator().hasNext();
        } else if (model instanceof TemplateHashModel) {
            return ((TemplateHashModel) model).isEmpty();
        } else if (model instanceof TemplateNumberModel
                || model instanceof TemplateDateModel
                || model instanceof TemplateBooleanModel) {
            return false;
        } else {
            return true;
        }
    }
}
