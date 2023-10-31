package freemarker.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.nodes.generated.Macro;
import freemarker.core.variables.*;

import static freemarker.core.variables.Wrap.*;
import static freemarker.core.variables.Constants.JAVA_NULL;;

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
			result = value instanceof WrappedDate;
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
			result = value instanceof Macro || value instanceof UserDirective;
		}
		else if (builtInName == "is_boolean") {
			result = isBoolean(value);
		}
		else if (builtInName == "is_hash") {
			result = value instanceof WrappedHash;
		}
		else if (builtInName == "is_hash_ex") {
			result = value instanceof WrappedHash;
		}
		else if (builtInName == "is_method") {
			result = value instanceof WrappedMethod;
		}
		else if (builtInName == "is_node") {
			result = value instanceof WrappedNode;
		}
		else if (builtInName == "is_null") {
			result = value == JAVA_NULL;
		}
		else if (builtInName == "is_transform") {
			result = false;
		}
		return result;
	}
}
