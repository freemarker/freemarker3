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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.template.*;
import freemarker.template.utility.StringUtil;

/**
 * Implementations of builtins for standard functions
 * that operate on sequences
 */

public class DateTime extends BuiltIn {
	

	public TemplateModel get(TemplateModel target, String builtInName, Environment env, BuiltInExpression callingExpression) throws TemplateException {
		int dateType = TemplateDateModel.DATETIME;
		if (builtInName == "date") {
			dateType = TemplateDateModel.DATE;
		}
		else if (builtInName == "time") {
			dateType = TemplateDateModel.TIME;
		}
		if (target instanceof TemplateDateModel) {
            TemplateDateModel dmodel = (TemplateDateModel) target;
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
		else if (target instanceof TemplateScalarModel) {
			return new DateParser(((TemplateScalarModel) target).getAsString(), dateType, callingExpression,  env);
		}
		else {
			throw callingExpression.invalidTypeException(target, callingExpression.getTarget(), env, "time/date or string");
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
        private BuiltInExpression callingExpression;
        private int dateType;
        private Date cachedValue;
        
        DateParser(String text, int dateType, BuiltInExpression callingExpression, Environment env)
        throws
            TemplateModelException
        {
            this.text = text;
            this.env = env;
            this.callingExpression = callingExpression;
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
                        "string?" + callingExpression.getName() + "(...) requires exactly 1 argument.");
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
                String mess = "Error: " + callingExpression.getStartLocation()
                             + "\nExpecting a date here, found: " + text;
                throw new TemplateModelException(mess);
            }
        }
    }
}
