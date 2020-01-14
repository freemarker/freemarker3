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

package freemarker.core.builtins;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.EvaluationUtil;
import freemarker.core.ast.TemplateNode;
import freemarker.template.*;

/**
 * Implementation of ?string built-in 
 */

public class stringBI extends ExpressionEvaluatingBuiltIn {
	
    @Override
    public boolean isSideEffectFree() {
        // For numbers, booleans, and dates, depends on actual format which can change. 
        return false;
    }
    
    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
        TemplateModel model) 
    throws TemplateException {
        if (model instanceof TemplateNumberModel) {
            return new NumberFormatter(EvaluationUtil.getNumber(model, caller.getTarget(), env), env);
        }
        if (model instanceof TemplateDateModel) {
            TemplateDateModel dm = (TemplateDateModel) model;
            int dateType = dm.getDateType();
            return new DateFormatter(EvaluationUtil.getDate(dm, caller.getTarget(), env), dateType, env);
        }
        if (model instanceof SimpleScalar) {
            return model;
        }
        if (model instanceof TemplateBooleanModel) {
            return new BooleanFormatter((TemplateBooleanModel) model, env);
        }
        if (model instanceof TemplateScalarModel) {
            return new SimpleScalar(((TemplateScalarModel) model).getAsString());
        } 
      	throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "number, date, or string");
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

	
