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
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

abstract public class BuiltIn {
    abstract public TemplateModel get(Environment env, BuiltInExpression caller) throws TemplateException;
    
    /**
     * True if the value this built-in returns from {@link #get(Environment, BuiltInExpression)} 
     * is free from side-effects - it does not depend on anything except its 
     * operand expression. Most built-ins are side-effect free and as such can 
     * be evaluated as literals at parse-time when their operands are also 
     * literals. Built-ins that depend on current environment settings are not
     * free of side-effects, i.e. ?contains, ?seq_index_of, ?seq_last_index_of,
     * ?sort, ?sort_by can depend on the environment's collator and/or 
     * arithmetic engine, ?string can depend on the environment's string 
     * formatting settings, and ?eval depends on variables defined in the 
     * scope. Most others are side-effect free though.  
     * @return true if the built-ins return value is free of side-effects.
     */
    public boolean isSideEffectFree() {
        return true;
    }
}
