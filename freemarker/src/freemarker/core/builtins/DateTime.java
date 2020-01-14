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
import java.util.Date;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.TemplateNode;
import freemarker.template.SimpleDate;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Implementations of builtins for standard functions
 * that operate on sequences
 * @version $Id: $
 */
public class DateTime extends ExpressionEvaluatingBuiltIn {

    private final int dateType;
    
    public DateTime(int dateType) {
        this.dateType = dateType;
    }

    @Override
    public boolean isSideEffectFree() {
        return false; // can depend on environment's date format
    }
    
    public TemplateModel get(Environment env, BuiltInExpression caller, 
            TemplateModel model) 
    throws TemplateException {
        if (model instanceof TemplateDateModel) {
            TemplateDateModel dmodel = (TemplateDateModel) model;
            int dtype = dmodel.getDateType();
            // Any date model can be coerced into its own type
            if(dateType == dtype) {
                return dmodel;
            }
            // unknown and datetime can be coerced into any date type
            if(dtype == TemplateDateModel.UNKNOWN || dtype == TemplateDateModel.DATETIME) {
                return new SimpleDate(dmodel.getAsDate(), dateType);
            }
            throw new TemplateException(
                    "Cannot convert " + TemplateDateModel.TYPE_NAMES.get(dtype)
                    + " into " + TemplateDateModel.TYPE_NAMES.get(dateType), env);
        }
        else if (model instanceof TemplateScalarModel) {
            return new DateParser(((TemplateScalarModel) model).getAsString(), 
                    dateType, caller,  env);
        }
        else {
            throw TemplateNode.invalidTypeException(model, caller, env, 
                    "time/date or string");
        }
    }

    static class DateParser
    implements
    TemplateDateModel,
    TemplateMethodModel,
    TemplateHashModel
    {
        private final String text;
        private final Environment env;
        private final DateFormat defaultFormat;
        private BuiltInExpression caller;
        private int dateType;
        private Date cachedValue;

        DateParser(String text, int dateType, BuiltInExpression caller, Environment env)
        throws
        TemplateModelException
        {
            this.text = text;
            this.env = env;
            this.caller = caller;
            this.dateType = dateType;
            this.defaultFormat = env.getDateFormatObject(dateType);
        }

        public Date getAsDate() throws TemplateModelException {
            if(cachedValue == null) {
                cachedValue = parse(defaultFormat);
            }
            return cachedValue;
        }

        public int getDateType() {
            return dateType;
        }

        public TemplateModel get(String pattern) throws TemplateModelException {
            return new SimpleDate(
                    parse(env.getDateFormatObject(dateType, pattern)),
                    dateType);
        }

        public Object exec(List arguments)
        throws TemplateModelException {
            if (arguments.size() != 1) {
                throw new TemplateModelException(
                        "string?" + caller.getName() + "(...) requires exactly 1 argument.");
            }
            return get((String) arguments.get(0));
        }

        public boolean isEmpty()
        {
            return false;
        }

        private Date parse(DateFormat df)
        throws
        TemplateModelException
        {
            try {
                return df.parse(text);
            }
            catch(java.text.ParseException e) {
                String mess = "Error: " + caller.getStartLocation()
                + "\nExpecting a date here, found: " + text;
                throw new TemplateModelException(mess);
            }
        }
    }
}
