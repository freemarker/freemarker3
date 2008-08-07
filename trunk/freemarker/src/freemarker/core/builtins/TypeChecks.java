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
 */

public class TypeChecks extends BuiltIn {
	
	public TemplateModel get(TemplateModel target, String builtInName, Environment env, BuiltInExpression callingExpression) throws TemplateException {
		boolean result = false;
		if (builtInName == "is_string") {
			result = target instanceof TemplateScalarModel;
		}
		else if (builtInName == "is_number") {
			result = target instanceof TemplateNumberModel;
		}
		else if (builtInName == "is_date") {
			result = target instanceof TemplateDateModel;
		}
		else if (builtInName == "is_enumerable") {
			result = target instanceof TemplateSequenceModel || target instanceof TemplateCollectionModel;
		}
		else if (builtInName == "is_sequence" || builtInName == "is_indexable") {
			result = target instanceof TemplateSequenceModel;
		}
		else if (builtInName == "is_macro") {
			result = (target instanceof Macro) && !((Macro) target).isFunction();
		}
		else if (builtInName == "is_directive") {
			result = target instanceof Macro || target instanceof TemplateTransformModel
                                 || target instanceof TemplateDirectiveModel;
		}
		else if (builtInName == "is_boolean") {
			result = target instanceof TemplateBooleanModel;
		}
		else if (builtInName == "is_hash") {
			result = target instanceof TemplateHashModel;
		}
		else if (builtInName == "is_hash_ex") {
			result = target instanceof TemplateHashModelEx;
		}
		else if (builtInName == "is_method") {
			result = target instanceof TemplateMethodModel;
		}
		else if (builtInName == "is_node") {
			result = target instanceof TemplateNodeModel;
		}
		else if (builtInName == "is_null") {
			result = target == TemplateModel.JAVA_NULL;
		}
		else if (builtInName == "is_transform") {
			result = target instanceof TemplateTransformModel;
		}
		else if (builtInName == "is_collection") {
			result = target instanceof TemplateCollectionModel;
		}
		return result ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
	}
}
