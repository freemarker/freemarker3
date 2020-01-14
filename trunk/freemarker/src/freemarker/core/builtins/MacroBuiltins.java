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
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.Macro;
import freemarker.core.ast.TemplateNode;
import freemarker.template.*;

/**
 * Implementations of ?scope and ?namespace built-ins
 * that expect  macro on the lhs.
 */

public abstract class MacroBuiltins extends ExpressionEvaluatingBuiltIn {

    @Override
    public boolean isSideEffectFree() {
        return false;
    }
    
    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) throws TemplateException {
        if (!(model instanceof Macro)) {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "macro");
        }
        return apply(env, (Macro)model);
    }
    
    public abstract TemplateModel apply(Environment env, Macro macro) 
    throws TemplateModelException;

    public static class Namespace extends MacroBuiltins {
        @Override
        public TemplateModel apply(Environment env, Macro macro)
                throws TemplateModelException
        {
            return env.getMacroNamespace(macro);
        }
    }

    public static class Scope extends MacroBuiltins {
        @Override
        public TemplateModel apply(Environment env, Macro macro)
                throws TemplateModelException
        {
            TemplateModel result = env.getMacroContext(macro);
            return result == null ? TemplateModel.JAVA_NULL : result;
        }
    }
}