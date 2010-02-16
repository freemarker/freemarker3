package freemarker.core.builtins;

import java.util.List;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.TemplateNode;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateSequenceModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class SequenceContainsBuiltIn extends ExpressionEvaluatingBuiltIn {

    @Override
    public boolean isSideEffectFree() {
        return false; // can depend on locale and arithmetic engine 
    }

    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) throws TemplateException
    {
        if (!(model instanceof TemplateSequenceModel || model instanceof TemplateCollectionModel)) {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "sequence or collection");
        }
        
        return new SequenceContainsFunction(model);
    }

    static class SequenceContainsFunction implements TemplateMethodModelEx {
        final TemplateSequenceModel sequence;
        final TemplateCollectionModel collection;
        SequenceContainsFunction(TemplateModel seqModel) {
            if (seqModel instanceof TemplateCollectionModel) {
                collection = (TemplateCollectionModel) seqModel;
                sequence = null;
            }
            else if (seqModel instanceof TemplateSequenceModel) {
                sequence = (TemplateSequenceModel) seqModel;
                collection = null;
            }
            else {
                throw new AssertionError();
            }
        }

        public TemplateModel exec(List args) throws TemplateModelException {
            if (args.size() != 1) {
                throw new TemplateModelException("Expecting exactly one argument for ?seq_contains(...)");
            }
            TemplateModel compareToThis = (TemplateModel) args.get(0);
            final ModelComparator modelComparator = new ModelComparator(Environment.getCurrentEnvironment());
            if (collection != null) {
                TemplateModelIterator tmi = collection.iterator();
                while (tmi.hasNext()) {
                    if (modelComparator.modelsEqual(tmi.next(), compareToThis)) {
                        return TemplateBooleanModel.TRUE;
                    }
                }
                return TemplateBooleanModel.FALSE;
            }
            else {
                for (int i=0; i<sequence.size(); i++) {
                    if (modelComparator.modelsEqual(sequence.get(i), compareToThis)) {
                        return TemplateBooleanModel.TRUE;
                    }
                }
                return TemplateBooleanModel.FALSE;
            }
        }
    }
}
