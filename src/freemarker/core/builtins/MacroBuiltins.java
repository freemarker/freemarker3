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
            TemplateModel model) {
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