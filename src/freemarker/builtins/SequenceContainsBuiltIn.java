package freemarker.builtins;

import java.util.Iterator;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.nodes.generated.TemplateNode;
import freemarker.core.variables.EvaluationException;
import freemarker.template.TemplateSequenceModel;
import freemarker.core.variables.Callable;

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

    static class SequenceContainsFunction implements Callable {
        final TemplateSequenceModel sequence;
        final Iterable<Object> collection;
        SequenceContainsFunction(Object seqModel) {
            if (seqModel instanceof Iterable) {
                collection = (Iterable<Object>) seqModel;
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

        public Boolean call(Object... args) {
            if (args.length != 1) {
                throw new EvaluationException("Expecting exactly one argument for ?seq_contains(...)");
            }
            Object compareToThis = args[0];
            final DefaultComparator modelComparator = new DefaultComparator(Environment.getCurrentEnvironment());
            if (collection != null) {
                Iterator<Object> tmi = collection.iterator();
                while (tmi.hasNext()) {
                    if (modelComparator.areEqual(tmi.next(), compareToThis)) {
                        return true;
                    }
                }
                return false;
            }
            else {
                for (int i=0; i<sequence.size(); i++) {
                    if (modelComparator.areEqual(sequence.get(i), compareToThis)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }
}
