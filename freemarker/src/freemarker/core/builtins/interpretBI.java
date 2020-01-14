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

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

/**
 * Implementation of ?interpret built-in 
 */
public class interpretBI extends ExpressionEvaluatingBuiltIn {

    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) 
    throws TemplateException {
        String id = null, interpretString = null;
        if (model instanceof TemplateSequenceModel) {
            TemplateSequenceModel tsm = (TemplateSequenceModel) model;
            TemplateModel tm = tsm.get(1);
            if (tm != null) {
                if(tm instanceof TemplateScalarModel) {
                    id = ((TemplateScalarModel) tm).getAsString();
                }
                else {
                    throw new TemplateModelException("Expecting string as second item of sequence of left of ?interpret built-in");
                }
            }
            tm = tsm.get(0);
            if (!(tm instanceof TemplateScalarModel)) {
                throw new TemplateModelException("Expecting string as first item of sequence of left of ?interpret built-in");
            }
            interpretString = ((TemplateScalarModel) tm).getAsString();
        }
        else if (model instanceof TemplateScalarModel) {
            interpretString = ((TemplateScalarModel) model).getAsString();
        }
        if (id == null) id = "anonymous_interpreted";
        if (interpretString == null) {
            throw new InvalidReferenceException("No string to interpret", env);
        }
        Template parentTemplate = env.getTemplate();
        try {
            Template template = new Template(parentTemplate.getName() + "$" + id, new StringReader(interpretString), parentTemplate.getConfiguration());
            template.setLocale(env.getLocale());
            return new TemplateProcessorModel(template);
        }
        catch(IOException e) {
            throw new TemplateException("", e, env);
        }
    }

    private static class TemplateProcessorModel implements TemplateDirectiveModel {
        private final Template template;

        TemplateProcessorModel(Template template) {
            this.template = template;
        }

        public void execute(Environment env, Map<String, TemplateModel> params,
                TemplateModel[] loopVars, TemplateDirectiveBody body)
                throws TemplateException, IOException {
            try {
                env.include(template, false);
            }
            catch(TemplateModelException e) {
                throw e;
            }
            catch(IOException e) {
                throw e;
            }
            catch(RuntimeException e) {
                throw e;
            }
            catch(Exception e) {
                throw new TemplateModelException(e);
            }
        }
    }
}
