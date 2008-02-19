/*
 * Copyright (c) 2007 The Visigoth Software Society. All rights
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

package freemarker.core.builtins;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.EvaluationUtil;
import freemarker.template.*;

/**
 * Implementation of ?string built-in 
 */

public class stringBI extends BuiltIn {
	
	public TemplateModel get(TemplateModel target, String builtInName, Environment env, BuiltInExpression callingExpression) throws TemplateException {
        if (target instanceof TemplateNumberModel) {
            return new NumberFormatter(EvaluationUtil.getNumber(target, callingExpression.getTarget(), env), env);
        }
        if (target instanceof TemplateDateModel) {
            TemplateDateModel dm = (TemplateDateModel) target;
            int dateType = dm.getDateType();
            return new DateFormatter(EvaluationUtil.getDate(dm, callingExpression.getTarget(), env), dateType, env);
        }
        if (target instanceof SimpleScalar) {
            return target;
        }
        if (target instanceof TemplateBooleanModel) {
            return new BooleanFormatter((TemplateBooleanModel) target, env);
        }
        if (target instanceof TemplateScalarModel) {
            return new SimpleScalar(((TemplateScalarModel) target).getAsString());
        } 
      	throw callingExpression.invalidTypeException(target, callingExpression.getTarget(), env, "number, date, or string");
	}
	
	
    static class BooleanFormatter
    implements TemplateScalarModel, LazilyEvaluatableArguments  
    {
        private final TemplateBooleanModel bool;
        private final Environment env;
        
        BooleanFormatter(TemplateBooleanModel bool, Environment env) {
            this.bool = bool;
            this.env = env;
        }

        public String getAsString() throws TemplateModelException {
            if (bool instanceof TemplateScalarModel) {
                return ((TemplateScalarModel) bool).getAsString();
            } else {
                return env.getBooleanFormat(bool.getAsBoolean());
            }
        }

        public Object exec(List arguments)
                throws TemplateModelException {
            if (arguments.size() != 2) {
                throw new TemplateModelException(
                        "boolean?string(...) requires exactly "
                        + "2 arguments.");
            }
            return new SimpleScalar(
                (String) arguments.get(bool.getAsBoolean() ? 0 : 1));
        }
    }
    
    
    static class DateFormatter
    implements TemplateScalarModel, TemplateHashModel, TemplateMethodModel {
        private final Date date;
        private final int dateType;
        private final Environment env;
        private final DateFormat defaultFormat;
        private String cachedValue;

        DateFormatter(Date date, int dateType, Environment env) throws TemplateModelException {
            this.date = date;
            this.dateType = dateType;
            this.env = env;
            defaultFormat = env.getDateFormatObject(dateType);
        }

        public String getAsString() throws TemplateModelException { 
            if(dateType == TemplateDateModel.UNKNOWN) {
                throw new TemplateModelException("Can't convert the date to string, because it is not known which parts of the date variable are in use. Use ?date, ?time or ?datetime built-in, or ?string.<format> or ?string(format) built-in with this date.");
            }
            if(cachedValue == null) {
                cachedValue = defaultFormat.format(date);
            }
            return cachedValue;
        }

        public TemplateModel get(String key)
        throws
            TemplateModelException
        {
            return new SimpleScalar(env.getDateFormatObject(dateType, key).format(date));
        }
        
        public Object exec(List arguments)
            throws TemplateModelException {
            if (arguments.size() != 1) {
                throw new TemplateModelException(
                        "date?string(...) requires exactly 1 argument.");
            }
            return get((String) arguments.get(0));
        }

        public boolean isEmpty()
        {
            return false;
        }
    }
    
    static class NumberFormatter implements TemplateScalarModel, TemplateHashModel, TemplateMethodModel {
        private final Number number;
        private final Environment env;
        private final NumberFormat defaultFormat;
        private String cachedValue;

        NumberFormatter(Number number, Environment env)
        {
            this.number = number;
            this.env = env;
            defaultFormat = env.getNumberFormatObject(env.getNumberFormat());
        }

        public String getAsString()
        {
            if(cachedValue == null) {
                cachedValue = defaultFormat.format(number);
            }
            return cachedValue;
        }

        public TemplateModel get(String key)
        {
            return new SimpleScalar(env.getNumberFormatObject(key).format(number));
        }
        
        @Parameters("format")
        
        public Object exec(List arguments)
            throws TemplateModelException {
            if (arguments.size() != 1) {
                throw new TemplateModelException(
                        "number?string(...) requires exactly 1 argument.");
            }
            return get((String) arguments.get(0));
        }

        public boolean isEmpty()
        {
            return false;
        }
    }
}

	
