package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.CollectionAndSequence;
import freemarker.core.ast.TemplateNode;
import freemarker.template.*;

/**
 * Implementation of ?resolve built-in 
 */

public abstract class HashBuiltin extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller,
            Object model) 
    {
        if (!(model instanceof TemplateHashModelEx)) {
            throw TemplateNode.invalidTypeException(model, 
                    caller.getTarget(), env, "extended hash");
        }
        final TemplateCollectionModel result = apply((TemplateHashModelEx) model);
        if (!(result instanceof TemplateSequenceModel)) {
            return new CollectionAndSequence(result);
        } 
        return result;
    }
    
    public abstract TemplateCollectionModel apply(TemplateHashModelEx hash) 
    throws TemplateModelException;
    
    public static class Keys extends HashBuiltin {
        @Override
        public TemplateCollectionModel apply(TemplateHashModelEx hash)
        {
            return hash.keys();
        }
    }

    public static class Values extends HashBuiltin {
        @Override
        public TemplateCollectionModel apply(TemplateHashModelEx hash)
        {
            return hash.values();
        }
    }
}
