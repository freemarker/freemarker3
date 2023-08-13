package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.parser.ast.BuiltInExpression;
import freemarker.core.ast.Macro;
import freemarker.template.*;

import static freemarker.ext.beans.ObjectWrapper.*;

/**
 * Implementation of ?is_XXXX built-ins
 * TODO: refactor into subclasses
 */

public class TypeChecks extends ExpressionEvaluatingBuiltIn {
	
    @Override
    public Object get(Environment env, BuiltInExpression caller,
        Object model) {
		boolean result = false;
		final String builtInName = caller.getName(); 
		if (builtInName == "is_string") {
			result = isString(model);
		}
		else if (builtInName == "is_number") {
			result = model instanceof TemplateNumberModel;
		}
		else if (builtInName == "is_date") {
			result = model instanceof TemplateDateModel;
		}
		else if (builtInName == "is_enumerable") {
			result = model instanceof TemplateSequenceModel || model instanceof Iterable;
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
			result = isBoolean(model);
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
			result = model == Constants.JAVA_NULL;
		}
		else if (builtInName == "is_transform") {
			result = model instanceof TemplateTransformModel;
		}
		else if (builtInName == "is_collection") {
			result = model instanceof Iterable;
		}
		return result;
	}
}
