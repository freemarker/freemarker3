package freemarker.core.builtins;

import java.util.Iterator;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.parser.ast.BuiltInExpression;
import freemarker.core.parser.ast.TemplateNode;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class SequenceContainsBuiltIn extends ExpressionEvaluatingBuiltIn {

    @Override
    public Object get(Environment env, BuiltInExpression caller,
            Object model) 
    {
        if (!(model instanceof TemplateSequenceModel || model instanceof Iterable)) {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "sequence or collection");
        }
        
        return new SequenceContainsFunction(model);
    }

    static class SequenceContainsFunction implements TemplateMethodModelEx {
        final TemplateSequenceModel sequence;
        final Iterable collection;
        SequenceContainsFunction(Object seqModel) {
            if (seqModel instanceof Iterable) {
                collection = (Iterable) seqModel;
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

        public Boolean exec(List args) {
            if (args.size() != 1) {
                throw new TemplateModelException("Expecting exactly one argument for ?seq_contains(...)");
            }
            Object compareToThis = args.get(0);
            final ModelComparator modelComparator = new ModelComparator(Environment.getCurrentEnvironment());
            if (collection != null) {
                Iterator<Object> tmi = collection.iterator();
                while (tmi.hasNext()) {
                    if (modelComparator.modelsEqual(tmi.next(), compareToThis)) {
                        return true;
                    }
                }
                return false;
            }
            else {
                for (int i=0; i<sequence.size(); i++) {
                    if (modelComparator.modelsEqual(sequence.get(i), compareToThis)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }
}
