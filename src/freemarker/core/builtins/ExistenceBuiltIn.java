package freemarker.core.builtins;

import java.util.List;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.Expression;
import freemarker.template.Constants;
import freemarker.template.LazilyEvaluatableArguments;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.core.parser.ast.ParentheticalExpression;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public abstract class ExistenceBuiltIn extends BuiltIn {
    public Object get(Environment env, BuiltInExpression caller) 
    {
        final Expression target = caller.getTarget();
        try {
            return apply(target.evaluate(env));
        }
        catch(InvalidReferenceException e) {
            if(!(target instanceof ParentheticalExpression)) {
                throw e;
            }
            return apply(null);
        }
    }

    public abstract Object apply(Object obj);

    public static final class DefaultBuiltIn extends ExistenceBuiltIn {
        public Object apply(final Object model) {
            if(model == null || model == Constants.JAVA_NULL) {
                return FirstDefined.INSTANCE;
            }
            return new TemplateMethodModelEx() {
                public Object exec(List arguments) {
                    return model;
                }
            };
        }
    };

    public static class IfExistsBuiltIn extends ExistenceBuiltIn {
        public Object apply(final Object model) {
            return model == null || model == Constants.JAVA_NULL ? Constants.NOTHING : model;
        }
    };

    public static class ExistsBuiltIn extends ExistenceBuiltIn {
        public Object apply(final Object model) {
            return model == null || model == Constants.JAVA_NULL ? TemplateBooleanModel.FALSE : TemplateBooleanModel.TRUE;
        }
    };
        
    public static class HasContentBuiltIn extends ExistenceBuiltIn {
        public Object apply(final Object model) {
            return model == null || Expression.isEmpty(model) ? TemplateBooleanModel.FALSE : TemplateBooleanModel.TRUE;
        }
    };
    public static class IsDefinedBuiltIn extends ExistenceBuiltIn {
        public Object apply(final Object model) {
            return model == null ? TemplateBooleanModel.FALSE : TemplateBooleanModel.TRUE;
        }
    };

    private static class FirstDefined implements TemplateMethodModelEx, LazilyEvaluatableArguments {
        static final FirstDefined INSTANCE = new FirstDefined();
        public Object exec(List args) {
            for (Object arg : args) {
                if (arg != null && arg != Constants.JAVA_NULL) {
                    return arg;
                }
            }
            return null;
        }
    };
}