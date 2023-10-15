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
    public Object get(Environment env, BuiltInExpression caller, Object value) {
		boolean result = false;
		final String builtInName = caller.getName(); 
		if (builtInName == "is_string") {
			result = isString(value);
		}
		else if (builtInName == "is_number") {
			result = isNumber(value);
		}
		else if (builtInName == "is_date") {
			result = value instanceof TemplateDateModel;
		}
		else if (builtInName == "is_enumerable" || builtInName == "is_collection") {
			result = isIterable(value);
		}
		else if (builtInName == "is_sequence" || builtInName == "is_indexable") {
			result = isList(value);
		}
		else if (builtInName == "is_macro") {
			result = (value instanceof Macro) && !((Macro) value).isFunction();
		}
		else if (builtInName == "is_directive") {
			result = value instanceof Macro || value instanceof TemplateDirectiveModel;
		}
		else if (builtInName == "is_boolean") {
			result = isBoolean(value);
		}
		else if (builtInName == "is_hash") {
			result = value instanceof TemplateHashModel;
		}
		else if (builtInName == "is_hash_ex") {
			result = value instanceof TemplateHashModelEx;
		}
		else if (builtInName == "is_method") {
			result = value instanceof TemplateMethodModel;
		}
		else if (builtInName == "is_node") {
			result = value instanceof TemplateNodeModel;
		}
		else if (builtInName == "is_null") {
			result = value == Constants.JAVA_NULL;
		}
		else if (builtInName == "is_transform") {
			result = false;
		}
		return result;
	}
}
