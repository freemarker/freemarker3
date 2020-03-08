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

import java.io.StringReader;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.Expression;
import freemarker.core.parser.FMConstants;
import freemarker.core.parser.FMLexer;
import freemarker.core.parser.FMParser;
import freemarker.core.parser.ParseException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Implementation of ?eval built-in 
 */

public class evalBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public boolean isSideEffectFree() {
        return false;
    }

    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) 
    throws TemplateException {
        try {
            return eval(((TemplateScalarModel) model).getAsString(), env, caller);
        } catch (ClassCastException cce) {
            throw new TemplateModelException("Expecting string on left of ?eval built-in");

        } catch (NullPointerException npe) {
            throw new TemplateModelException(npe);
        }
    }

    TemplateModel eval(String s, Environment env, BuiltInExpression caller) 
    throws TemplateException {
//        SimpleCharStream scs = new SimpleCharStream(
//                new StringReader("(" + s + ")"), caller.getBeginLine(),
//                caller.getBeginColumn(), 16*s.length());
        //        FMLexer token_source = new FMLexer(scs);
        StringReader sr = new StringReader("(" + s + ")");
        FMLexer token_source = new FMLexer(sr, FMConstants.EXPRESSION, caller.getBeginLine(), caller.getBeginColumn());
//        token_source.SwitchTo(FMConstants.EXPRESSION);
        FMParser parser = new FMParser(token_source);
        parser.setTemplate(caller.getTemplate());
        Expression exp = null;
        try {
            exp = parser.Exp();
        } catch (ParseException pe) {
            pe.setTemplateName(caller.getTemplate().getName());
            throw new TemplateException(pe, env);
        }
        return exp.getAsTemplateModel(env);
    }
}