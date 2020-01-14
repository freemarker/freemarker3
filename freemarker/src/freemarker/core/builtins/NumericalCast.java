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

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.TemplateNode;
import freemarker.template.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Implementation of ?byte, ?int, ?double, ?float,
 * ?short and ?long built-ins 
 */

public class NumericalCast extends ExpressionEvaluatingBuiltIn {
    private static final BigDecimal half = new BigDecimal("0.5");
    private static final MathContext mc = new MathContext(0, RoundingMode.FLOOR);

    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) 
    throws TemplateException {
        try {
            return new SimpleNumber(getNumber(((TemplateNumberModel) model).getAsNumber(), caller.getName()));
        } catch (ClassCastException cce) {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "number");
        } catch (NullPointerException npe) {
            throw new InvalidReferenceException("Undefined number", env);
        }
    }

    private Number getNumber(Number num, String builtInName) {
        if (builtInName == "int") {
            return Integer.valueOf(num.intValue());
        }
        else if (builtInName == "double") {
            return new Double(num.doubleValue());
        }
        else if (builtInName == "long") {
            return Long.valueOf(num.longValue());
        }
        else if (builtInName == "float") {
            return new Float(num.floatValue());
        }
        else if (builtInName == "byte") {
            return Byte.valueOf(num.byteValue());
        }
        else if (builtInName == "short") {
            return Short.valueOf(num.shortValue());
        }
        else if (builtInName == "floor") {
            return (BigDecimal.valueOf(num.doubleValue()).divide(BigDecimal.ONE, 0, RoundingMode.FLOOR));
        }
        else if (builtInName == "ceiling") {
            return (BigDecimal.valueOf(num.doubleValue()).divide(BigDecimal.ONE, 0, RoundingMode.CEILING));
        }
        else if (builtInName == "round") {
            return (BigDecimal.valueOf(num.doubleValue()).add(half, mc).divide(BigDecimal.ONE, 0, RoundingMode.FLOOR));
        }
        else {
            throw new InternalError("The only numerical cast built-ins available are ?int, ?long, ?short, ?byte, ?float, ?double, ?floor, ?ceiling, and ?round.");
        }
    }
}
