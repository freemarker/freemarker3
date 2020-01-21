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

import java.text.Collator;
import java.util.Date;

import freemarker.core.Environment;
import freemarker.core.ast.ArithmeticEngine;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class ModelComparator
{
    private final ArithmeticEngine arithmeticEngine;
    private final Collator collator;
    
    public ModelComparator(Environment env) {
        arithmeticEngine = env.getArithmeticEngine();
        collator = env.getCollator();
    }
    
    /*
     * WARNING! This algorithm is duplication of ComparisonExpression.isTrue(...).
     * Thus, if you update this method, then you have to update that too!
     */
    public boolean modelsEqual(TemplateModel model1, TemplateModel model2)
    throws TemplateModelException {
        if(model1 instanceof TemplateNumberModel && model2 instanceof TemplateNumberModel) {
            try {
                return arithmeticEngine.compareNumbers(
                        ((TemplateNumberModel) model1).getAsNumber(), 
                        ((TemplateNumberModel) model2).getAsNumber()) == 0;
            } catch (TemplateException ex) {
                throw new TemplateModelException(ex);
            }
        }
        
        if(model1 instanceof TemplateDateModel && model2 instanceof TemplateDateModel) {
            TemplateDateModel ltdm = (TemplateDateModel)model1;
            TemplateDateModel rtdm = (TemplateDateModel)model2;
            int ltype = ltdm.getDateType();
            int rtype = rtdm.getDateType();
            if(ltype != rtype) {
                throw new TemplateModelException(
                        "Can not compare dates of different type. Left date is of "
                        + TemplateDateModel.TYPE_NAMES.get(ltype)
                        + " type, right date is of "
                        + TemplateDateModel.TYPE_NAMES.get(rtype) + " type.");
            }
            if(ltype == TemplateDateModel.UNKNOWN) {
                throw new TemplateModelException(
                "Left date is of UNKNOWN type, and can not be compared.");
            }
            if(rtype == TemplateDateModel.UNKNOWN) {
                throw new TemplateModelException(
                "Right date is of UNKNOWN type, and can not be compared.");
            }
            Date first = ltdm.getAsDate();
            Date second = rtdm.getAsDate();
            return first.compareTo(second) == 0;
        }
        
        if(model1 instanceof TemplateScalarModel && model2 instanceof TemplateScalarModel) {
            return collator.compare(
                    ((TemplateScalarModel) model1).getAsString(), 
                    ((TemplateScalarModel) model2).getAsString()) == 0;
        }
        
        if(model1 instanceof TemplateBooleanModel && model2 instanceof TemplateBooleanModel) {
            return ((TemplateBooleanModel)model1).getAsBoolean() == ((TemplateBooleanModel)model2).getAsBoolean();
        }
        return false;
    }
}