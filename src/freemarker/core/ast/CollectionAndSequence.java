package freemarker.core.ast;

import freemarker.ext.beans.ListModel;
import freemarker.template.*;
import java.util.*;

/**
 * Add sequence capabilities to an existing collection, or
 * vice versa. Used by ?keys and ?values built-ins.
 */
final public class CollectionAndSequence implements Iterable, TemplateSequenceModel {

    private Iterable<?> collection;
    private TemplateSequenceModel sequence;
    private ArrayList<Object> data;

    public CollectionAndSequence(Iterable<?> collection) {
        this.collection = collection;
    }

    public CollectionAndSequence(TemplateSequenceModel sequence) {
        this.sequence = sequence;
    }
    
    public CollectionAndSequence(ListModel listModel) {
        this.sequence = listModel;
    }

    public Iterator<?> iterator() {
        if (collection != null) {
            return collection.iterator();
        } else {
            return new SequenceIterator(sequence);
        }
    }

    public Object get(int i) {
        if (sequence != null) {
            return sequence.get(i);
        } else {
            initSequence();
            return data.get(i);
        }
    }

    public int size() {
        if (sequence != null) {
            return sequence.size();
        } else {
            initSequence();
            return data.size();
        }
    }

    private void initSequence() {
        if (data == null) {
            data = new ArrayList<Object>();
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                data.add(it.next());
            }
        }
    }

    private static class SequenceIterator
    implements Iterator<Object>
    {
        private final TemplateSequenceModel sequence;
        private final int size;
        private int index = 0;

        SequenceIterator(TemplateSequenceModel sequence) {
            this.sequence = sequence;
            this.size = sequence.size();
            
        }
        public Object next() {
            return sequence.get(index++);
        }

        public boolean hasNext() {
            return index < size;
        }
    }
}
