package freemarker.core.builtins;

import java.util.List;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.Expression;
import freemarker.core.ast.ParentheticalExpression;
import freemarker.template.LazilyEvaluatableArguments;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public abstract class ExistenceBuiltIn extends BuiltIn {
    public TemplateModel get(Environment env, BuiltInExpression caller) 
    {
        final Expression target = caller.getTarget();
        try {
            return apply(target.getAsTemplateModel(env));
        }
        catch(InvalidReferenceException e) {
            if(!(target instanceof ParentheticalExpression)) {
                throw e;
            }
            return apply(null);
        }
    }

    public abstract TemplateModel apply(TemplateModel model) throws TemplateModelException;

    public static final class DefaultBuiltIn extends ExistenceBuiltIn {
        public TemplateModel apply(final TemplateModel model) {
            if(model == null || model == TemplateModel.JAVA_NULL) {
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
        public TemplateModel apply(final TemplateModel model) {
            return model == null || model == TemplateModel.JAVA_NULL ? TemplateModel.NOTHING : model;
        }
    };

    public static class ExistsBuiltIn extends ExistenceBuiltIn {
        public TemplateModel apply(final TemplateModel model) {
            return model == null || model == TemplateModel.JAVA_NULL ? TemplateBooleanModel.FALSE : TemplateBooleanModel.TRUE;
        }
    };
        
    public static class HasContentBuiltIn extends ExistenceBuiltIn {
        public TemplateModel apply(final TemplateModel model) {
            return model == null || Expression.isEmpty(model) ? TemplateBooleanModel.FALSE : TemplateBooleanModel.TRUE;
        }
    };
    public static class IsDefinedBuiltIn extends ExistenceBuiltIn {
        public TemplateModel apply(final TemplateModel model) {
            return model == null ? TemplateBooleanModel.FALSE : TemplateBooleanModel.TRUE;
        }
    };

    private static class FirstDefined implements TemplateMethodModelEx, LazilyEvaluatableArguments {
        static final FirstDefined INSTANCE = new FirstDefined();
        public TemplateModel exec(List args) {
            for (Object arg : args) {
                if (arg != null && arg != TemplateModel.JAVA_NULL) {
                    return (TemplateModel) arg;
                }
            }
            return null;
        }
    };
}