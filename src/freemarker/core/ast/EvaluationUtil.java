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

import java.util.Date;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.template.*;

/**
 * @version 1.0
 * @author Attila Szegedi
 */
public class EvaluationUtil
{
    private EvaluationUtil()
    {
    }
    
    static String getString(TemplateScalarModel model, Expression expr, Environment env)
    throws
        TemplateException
    {
        String value = model.getAsString();
        if(value == null) {
            throw new TemplateException(expr + " evaluated to null string.", env);
        }
        return value;
    }

    static Number getNumber(Expression expr, Environment env)
    throws
        TemplateException
    {
        TemplateModel model = expr.getAsTemplateModel(env);
        return getNumber(model, expr, env);
    }

    static public Number getNumber(TemplateModel model, Expression expr, Environment env)
    throws
        TemplateException
    {
        if(model instanceof TemplateNumberModel) {
            return getNumber((TemplateNumberModel)model, expr, env);
        }
        else if(model == null) {
            throw new InvalidReferenceException(expr + " is undefined.", env);
        }
        else if(model == TemplateModel.JAVA_NULL) {
            throw new InvalidReferenceException(expr + " is null.", env);
        }
        else {
            throw new NonNumericalException(expr + " is not a number, it is " + model.getClass().getName(), env);
        }
    }

    static Number getNumber(TemplateNumberModel model, Expression expr, Environment env)
        throws TemplateModelException, TemplateException
    {
        Number value = model.getAsNumber();
        if(value == null) {
            throw new TemplateException(expr + " evaluated to null number.", env);
        }
        return value;
    }

    static public Date getDate(TemplateDateModel model, Expression expr, Environment env)
        throws TemplateModelException, TemplateException
    {
        Date value = model.getAsDate();
        if(value == null) {
            throw new TemplateException(expr + " evaluated to null date.", env);
        }
        return value;
    }
}
