package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.Macro;
import freemarker.core.parser.ast.TemplateNode;
import freemarker.template.Constants;

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
    public Object get(Environment env, BuiltInExpression caller,
            Object model) {
        if (!(model instanceof Macro)) {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "macro");
        }
        return apply(env, (Macro)model);
    }
    
    public abstract Object apply(Environment env, Macro macro);

    public static class Namespace extends MacroBuiltins {
        @Override
        public Object apply(Environment env, Macro macro)
        {
            return env.getMacroNamespace(macro);
        }
    }

    public static class Scope extends MacroBuiltins {
        @Override
        public Object apply(Environment env, Macro macro)
        {
            Object result = env.getMacroContext(macro);
            return result == null ? Constants.JAVA_NULL : result;
        }
    }
}