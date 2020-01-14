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
import freemarker.template.*;

/**
 * Implementation of ?is_XXXX built-ins
 * TODO: refactor into subclasses
 */

public class TypeChecks extends ExpressionEvaluatingBuiltIn {
	
    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
        TemplateModel model) throws TemplateException {
		boolean result = false;
		final String builtInName = caller.getName(); 
		if (builtInName == "is_string") {
			result = model instanceof TemplateScalarModel;
		}
		else if (builtInName == "is_number") {
			result = model instanceof TemplateNumberModel;
		}
		else if (builtInName == "is_date") {
			result = model instanceof TemplateDateModel;
		}
		else if (builtInName == "is_enumerable") {
			result = model instanceof TemplateSequenceModel || model instanceof TemplateCollectionModel;
		}
		else if (builtInName == "is_sequence" || builtInName == "is_indexable") {
			result = model instanceof TemplateSequenceModel;
		}
		else if (builtInName == "is_macro") {
			result = (model instanceof Macro) && !((Macro) model).isFunction();
		}
		else if (builtInName == "is_directive") {
			result = model instanceof Macro || model instanceof TemplateTransformModel
                                 || model instanceof TemplateDirectiveModel;
		}
		else if (builtInName == "is_boolean") {
			result = model instanceof TemplateBooleanModel;
		}
		else if (builtInName == "is_hash") {
			result = model instanceof TemplateHashModel;
		}
		else if (builtInName == "is_hash_ex") {
			result = model instanceof TemplateHashModelEx;
		}
		else if (builtInName == "is_method") {
			result = model instanceof TemplateMethodModel;
		}
		else if (builtInName == "is_node") {
			result = model instanceof TemplateNodeModel;
		}
		else if (builtInName == "is_null") {
			result = model == TemplateModel.JAVA_NULL;
		}
		else if (builtInName == "is_transform") {
			result = model instanceof TemplateTransformModel;
		}
		else if (builtInName == "is_collection") {
			result = model instanceof TemplateCollectionModel;
		}
		return result ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
	}
}
