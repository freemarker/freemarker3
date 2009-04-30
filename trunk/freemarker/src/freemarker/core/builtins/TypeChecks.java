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
