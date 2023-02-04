package freemarker.core.ast;

import freemarker.template.*;
import java.util.*;
import java.io.Serializable;

/**
 * Add sequence capabilities to an existing collection, or
 * vice versa. Used by ?keys and ?values built-ins.
 */
final public class CollectionAndSequence
implements TemplateCollectionModel, TemplateSequenceModel, Serializable
{
    private static final long serialVersionUID = -4474902410323664315L;

    private TemplateCollectionModel collection;
    private TemplateSequenceModel sequence;
    private ArrayList<TemplateModel> data;

    public CollectionAndSequence(TemplateCollectionModel collection) {
        this.collection = collection;
    }

    public CollectionAndSequence(TemplateSequenceModel sequence) {
        this.sequence = sequence;
    }

    public TemplateModelIterator iterator() throws TemplateModelException {
        if (collection != null) {
            return collection.iterator();
        } else {
            return new SequenceIterator(sequence);
        }
    }

    public TemplateModel get(int i) throws TemplateModelException {
        if (sequence != null) {
            return sequence.get(i);
        } else {
            initSequence();
            return data.get(i);
        }
    }

    public int size() throws TemplateModelException {
        if (sequence != null) {
            return sequence.size();
        } else {
            initSequence();
            return data.size();
        }
    }

    private void initSequence() throws TemplateModelException {
        if (data == null) {
            data = new ArrayList<TemplateModel>();
            TemplateModelIterator it = collection.iterator();
            while (it.hasNext()) {
                data.add(it.next());
            }
        }
    }

    private static class SequenceIterator
    implements TemplateModelIterator
    {
        private final TemplateSequenceModel sequence;
        private final int size;
        private int index = 0;

        SequenceIterator(TemplateSequenceModel sequence) throws TemplateModelException {
            this.sequence = sequence;
            this.size = sequence.size();
            
        }
        public TemplateModel next() throws TemplateModelException {
            return sequence.get(index++);
        }

        public boolean hasNext() {
            return index < size;
        }
    }
}
