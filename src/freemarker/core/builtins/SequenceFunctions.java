package freemarker.core.builtins;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.ast.ArithmeticEngine;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.TemplateNode;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelListSequence;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;
import freemarker.template.utility.StringUtil;

/**
 * Implementations of builtins for standard functions that operate on sequences
 * TODO: refactor properly into subclasses
 * TODO: see whether various result models really need env field
 */

public abstract class SequenceFunctions extends ExpressionEvaluatingBuiltIn {

    static final int KEY_TYPE_STRING = 1;
    static final int KEY_TYPE_NUMBER = 2;
    static final int KEY_TYPE_DATE = 3;


    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) 
    {
        if (!(model instanceof TemplateSequenceModel)) {
            throw TemplateNode.invalidTypeException(model,
                    caller.getTarget(), env, "sequence");
        }
        return apply((TemplateSequenceModel) model);
    }
    
    public abstract TemplateModel apply(TemplateSequenceModel model) throws TemplateException;
    
    public static class First extends SequenceFunctions {
        @Override
        public TemplateModel apply(TemplateSequenceModel sequence) {
            return sequence.size() > 0 ? sequence.get(0) : null;
        }
    }

    public static class Last extends SequenceFunctions {
        @Override
        public TemplateModel apply(TemplateSequenceModel sequence) {
            return sequence.size() > 0 ? sequence.get(sequence.size() - 1) : null;
        }
    }

    public static class Reverse extends SequenceFunctions {
        @Override
        public TemplateModel apply(TemplateSequenceModel sequence) {
            return new ReverseSequence(sequence);
        }
    }

    public static class Sort extends SequenceFunctions {
        @Override
        public boolean isSideEffectFree() {
            return false; // depends on locale and arithmetic engine
        }
        
        @Override
        public TemplateModel apply(TemplateSequenceModel sequence) {
            return sort(sequence, null);
        }
    }

    public static class SortBy extends SequenceFunctions {
        @Override
        public boolean isSideEffectFree() {
            return false; // depends on locale and arithmetic engine
        }

        @Override
        public TemplateModel apply(TemplateSequenceModel sequence) {
            return new SortByMethod(sequence);
        }
    }

    public static class Chunk extends SequenceFunctions {
        @Override
        public TemplateModel apply(TemplateSequenceModel sequence) {
            return new ChunkFunction(sequence);
        }
    }

    public static class IndexOf extends SequenceFunctions {
        @Override
        public boolean isSideEffectFree() {
            return false; // can depend on locale and arithmetic engine 
        }
        
        @Override
        public TemplateModel apply(TemplateSequenceModel sequence) {
            return new SequenceIndexOf(sequence, false);
        }
    }

    public static class LastIndexOf extends SequenceFunctions {
        @Override
        public boolean isSideEffectFree() {
            return false; // can depend on locale and arithmetic engine 
        }

        @Override
        public TemplateModel apply(TemplateSequenceModel sequence) {
            return new SequenceIndexOf(sequence, true);
        }
    }

    static class ReverseSequence implements TemplateSequenceModel {
        private final TemplateSequenceModel seq;

        ReverseSequence(TemplateSequenceModel seq) {
            this.seq = seq;
        }

        public int size() {
            return seq.size();
        }

        public TemplateModel get(int index) {
            return seq.get(seq.size() - 1 - index);
        }
    }

    static class ChunkFunction implements TemplateMethodModelEx {

        private final TemplateSequenceModel tsm;

        private ChunkFunction(TemplateSequenceModel tsm) {
            this.tsm = tsm;
        }
        
        public Object exec(List args) {
            int numArgs = args.size();
            if (numArgs != 1 && numArgs != 2) {
                throw new TemplateModelException(
                "?chunk(...) expects 1 or 2 arguments.");
            }

            Object chunkSize = args.get(0);
            if (!(chunkSize instanceof TemplateNumberModel)) {
                throw new TemplateModelException(
                        "?chunk(...) expects a number as "
                        + "its 1st argument.");
            }

            return new ChunkedSequence(tsm, ((TemplateNumberModel) chunkSize)
                    .getAsNumber().intValue(),
                    numArgs > 1 ? (TemplateModel) args.get(1) : null);
        }
    }

    static class ChunkedSequence implements TemplateSequenceModel {

        private final TemplateSequenceModel wrappedTsm;

        private final int chunkSize;

        private final TemplateModel fillerItem;

        private final int numberOfChunks;

        private ChunkedSequence(TemplateSequenceModel wrappedTsm,
                int chunkSize, TemplateModel fillerItem)
        {
            if (chunkSize < 1) {
                throw new TemplateModelException(
                "The 1st argument to ?chunk(...) must be at least 1.");
            }
            this.wrappedTsm = wrappedTsm;
            this.chunkSize = chunkSize;
            this.fillerItem = fillerItem;
            numberOfChunks = (wrappedTsm.size() + chunkSize - 1) / chunkSize;
        }

        public TemplateModel get(final int chunkIndex)
        {
            if (chunkIndex >= numberOfChunks) {
                return null;
            }

            return new TemplateSequenceModel() {

                private final int baseIndex = chunkIndex * chunkSize;

                public TemplateModel get(int relIndex)
                {
                    int absIndex = baseIndex + relIndex;
                    if (absIndex < wrappedTsm.size()) {
                        return wrappedTsm.get(absIndex);
                    } else {
                        return absIndex < numberOfChunks * chunkSize ? fillerItem
                                : null;
                    }
                }

                public int size() {
                    return fillerItem != null
                    || chunkIndex + 1 < numberOfChunks ? chunkSize
                            : wrappedTsm.size() - baseIndex;
                }

            };
        }

        public int size() {
            return numberOfChunks;
        }

    }

    static TemplateSequenceModel sort(TemplateSequenceModel seq, String[] keys)
    {
        int i;
        int keyCnt;

        int ln = seq.size();
        if (ln == 0) {
            return seq;
        }

        List<Object> res = new ArrayList<Object>(ln);
        Object item;
        item = seq.get(0);
        if (keys != null) {
            keyCnt = keys.length;
            if (keyCnt == 0) {
                keys = null;
            } else {
                for (i = 0; i < keyCnt; i++) {
                    if (!(item instanceof TemplateHashModel)) {
                        throw new TemplateModelException(
                                "sorting failed: "
                                + (i == 0 ? "You can't use ?sort_by when the "
                                        + "sequence items are not hashes."
                                        : "The subvariable "
                                            + StringUtil
                                            .jQuote(keys[i - 1])
                                            + " is not a hash, so ?sort_by "
                                            + "can't proceed by getting the "
                                            + StringUtil
                                            .jQuote(keys[i])
                                            + " subvariable."));
                    }

                    item = ((TemplateHashModel) item).get(keys[i]);
                    if (item == null) {
                        throw new TemplateModelException(
                                "sorting failed: "
                                + "The "
                                + StringUtil.jQuote(keys[i])
                                + " subvariable "
                                + (keyCnt == 1 ? "was not found."
                                        : "(specified by ?sort_by argument number "
                                            + (i + 1)
                                            + ") was not found."));
                    }
                }
            }
        } else {
            keyCnt = 0;
        }

        int keyType;
        if (item instanceof TemplateScalarModel) {
            keyType = KEY_TYPE_STRING;
        } else if (item instanceof TemplateNumberModel) {
            keyType = KEY_TYPE_NUMBER;
        } else if (item instanceof TemplateDateModel) {
            keyType = KEY_TYPE_DATE;
        } else {
            throw new TemplateModelException(
                    "sorting failed: "
                    + "Values used for sorting must be numbers, strings, or date/time values.");
        }

        if (keys == null) {
            if (keyType == KEY_TYPE_STRING) {
                for (i = 0; i < ln; i++) {
                    item = seq.get(i);
                    try {
                        res.add(new KVP(((TemplateScalarModel) item)
                                .getAsString(), item));
                    } catch (ClassCastException e) {
                        if (!(item instanceof TemplateScalarModel)) {
                            throw new TemplateModelException(
                                    "Failure of ?sort built-in: "
                                    + "All values in the sequence must be "
                                    + "strings, because the first value "
                                    + "was a string. "
                                    + "The value at index " + i
                                    + " is not string.");
                        } else {
                            throw e;
                        }
                    }
                }
            } else if (keyType == KEY_TYPE_NUMBER) {
                for (i = 0; i < ln; i++) {
                    item = seq.get(i);
                    try {
                        res.add(new KVP(((TemplateNumberModel) item)
                                .getAsNumber(), item));
                    } catch (ClassCastException e) {
                        if (!(item instanceof TemplateNumberModel)) {
                            throw new TemplateModelException(
                                    "sorting failed: " 
                                    + "All values in the sequence must be "
                                    + "numbers, because the first value "
                                    + "was a number. "
                                    + "The value at index " + i
                                    + " is not number.");
                        } else {
                            throw e;
                        }
                    }
                }
            } else if (keyType == KEY_TYPE_DATE) {
                for (i = 0; i < ln; i++) {
                    item = seq.get(i);
                    try {
                        res.add(new KVP(((TemplateDateModel) item).getAsDate(),
                                item));
                    } catch (ClassCastException e) {
                        if (!(item instanceof TemplateNumberModel)) {
                            throw new TemplateModelException(
                                    "sorting failed: " 
                                    + "All values in the sequence must be "
                                    + "date/time values, because the first "
                                    + "value was a date/time. "
                                    + "The value at index " + i
                                    + " is not date/time.");
                        } else {
                            throw e;
                        }
                    }
                }
            } else {
                throw new RuntimeException("FreeMarker bug: Bad key type");
            }
        } else {
            for (i = 0; i < ln; i++) {
                item = seq.get(i);
                Object key = item;
                for (int j = 0; j < keyCnt; j++) {
                    try {
                        key = ((TemplateHashModel) key).get(keys[j]);
                    } catch (ClassCastException e) {
                        if (!(key instanceof TemplateHashModel)) {
                            throw new TemplateModelException(
                                    "sorting failed: " 
                                    + "Problem with the sequence item at index "
                                    + i
                                    + ": "
                                    + "Can't get the "
                                    + StringUtil.jQuote(keys[j])
                                    + " subvariable, because the value is not a hash.");
                        } else {
                            throw e;
                        }
                    }
                    if (key == null) {
                        throw new TemplateModelException(
                                "sorting failed "  
                                + "Problem with the sequence item at index "
                                + i + ": " + "The "
                                + StringUtil.jQuote(keys[j])
                                + " subvariable was not found.");
                    }
                }
                if (keyType == KEY_TYPE_STRING) {
                    try {
                        res.add(new KVP(((TemplateScalarModel) key)
                                .getAsString(), item));
                    } catch (ClassCastException e) {
                        if (!(key instanceof TemplateScalarModel)) {
                            throw new TemplateModelException(
                                    "sorting failed: " 
                                    + "All key values in the sequence must be "
                                    + "date/time values, because the first key "
                                    + "value was a date/time. The key value at "
                                    + "index " + i
                                    + " is not a date/time.");
                        } else {
                            throw e;
                        }
                    }
                } else if (keyType == KEY_TYPE_NUMBER) {
                    try {
                        res.add(new KVP(((TemplateNumberModel) key)
                                .getAsNumber(), item));
                    } catch (ClassCastException e) {
                        if (!(key instanceof TemplateNumberModel)) {
                            throw new TemplateModelException(
                                    "sorting failed: "
                                    + "All key values in the sequence must be "
                                    + "numbers, because the first key "
                                    + "value was a number. The key value at "
                                    + "index " + i
                                    + " is not a number.");
                        }
                    }
                } else if (keyType == KEY_TYPE_DATE) {
                    try {
                        res.add(new KVP(((TemplateDateModel) key).getAsDate(),
                                item));
                    } catch (ClassCastException e) {
                        if (!(key instanceof TemplateDateModel)) {
                            throw new TemplateModelException(
                                    "sorting failed: "
                                    + "All key values in the sequence must be "
                                    + "dates, because the first key "
                                    + "value was a date. The key value at "
                                    + "index " + i + " is not a date.");
                        }
                    }
                } else {
                    throw new RuntimeException("FreeMarker bug: Bad key type");
                }
            }
        }

        Comparator cmprtr;
        if (keyType == KEY_TYPE_STRING) {
            cmprtr = new LexicalKVPComparator(Environment
                    .getCurrentEnvironment().getCollator());
        } else if (keyType == KEY_TYPE_NUMBER) {
            cmprtr = new NumericalKVPComparator(Environment
                    .getCurrentEnvironment().getArithmeticEngine());
        } else if (keyType == KEY_TYPE_DATE) {
            cmprtr = DateKVPComparator.INSTANCE;
        } else {
            throw new RuntimeException("FreeMarker bug: Bad key type");
        }

        try {
            Collections.sort(res, cmprtr);
        } catch (ClassCastException exc) {
            throw new TemplateModelException("Unexpected error while sorting:" + exc, exc);
        }

        for (i = 0; i < ln; i++) {
            res.set(i, ((KVP) res.get(i)).value);
        }

        return new TemplateModelListSequence(res);
    }

    static class KVP {
        private KVP(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        private Object key;
        private Object value;
    }

    static class NumericalKVPComparator implements Comparator {
        private ArithmeticEngine ae;

        private NumericalKVPComparator(ArithmeticEngine ae) {
            this.ae = ae;
        }

        public int compare(Object arg0, Object arg1) {
            try {
                return ae.compareNumbers(
                        (Number) ((KVP) arg0).key,
                        (Number) ((KVP) arg1).key);
            } catch (TemplateException e) {
                throw new ClassCastException(
                        "Failed to compare numbers: " + e);
            }
        }
    }

    static class LexicalKVPComparator implements Comparator {
        private Collator collator;

        LexicalKVPComparator(Collator collator) {
            this.collator = collator;
        }

        public int compare(Object arg0, Object arg1) {
            return collator.compare(
                    ((KVP) arg0).key, ((KVP) arg1).key);
        }
    }

    static class DateKVPComparator implements Comparator {
        static final DateKVPComparator INSTANCE = new DateKVPComparator();
        public int compare(Object arg0, Object arg1) {
            return ((Date) ((KVP) arg0).key).compareTo(
                    (Date) ((KVP) arg1).key);
        }
    }

    static class SortByMethod implements TemplateMethodModelEx {
        TemplateSequenceModel seq;

        SortByMethod(TemplateSequenceModel seq) {
            this.seq = seq;
        }

        public Object exec(List params)
        {
            if (params.size() == 0) {
                throw new TemplateModelException(
                        "?sort_by(key) needs exactly 1 argument.");
            }
            String[] subvars;
            Object obj = params.get(0);
            if (obj instanceof TemplateScalarModel) {
                subvars = new String[]{((TemplateScalarModel) obj).getAsString()};
            } else if (obj instanceof TemplateSequenceModel) {
                TemplateSequenceModel seq = (TemplateSequenceModel) obj;
                int ln = seq.size();
                subvars = new String[ln];
                for (int i = 0; i < ln; i++) {
                    Object item = seq.get(i);
                    try {
                        subvars[i] = ((TemplateScalarModel) item)
                        .getAsString();
                    } catch (ClassCastException e) {
                        if (!(item instanceof TemplateScalarModel)) {
                            throw new TemplateModelException(
                                    "The argument to ?sort_by(key), when it "
                                    + "is a sequence, must be a sequence of "
                                    + "strings, but the item at index " + i
                                    + " is not a string." );
                        }
                    }
                }
            } else {
                throw new TemplateModelException(
                        "The argument to ?sort_by(key) must be a string "
                        + "(the name of the subvariable), or a sequence of "
                        + "strings (the \"path\" to the subvariable).");
            }
            return sort(seq, subvars); 
        }
    }

    static class SequenceIndexOf implements TemplateMethodModelEx {

        private final TemplateSequenceModel sequence;
        private final boolean reverse;

        SequenceIndexOf(TemplateSequenceModel sequence, boolean reverse) {
            this.sequence = sequence;
            this.reverse = reverse;
        }

        public TemplateModel exec(List args) {
            final int argc = args.size();
            final int startIndex;
            if (argc != 1 && argc != 2) {
                throw new TemplateModelException("Expecting one or two arguments for ?seq_" + (reverse ? "last_" : "") + "index_of");
            }
            TemplateModel compareToThis = (TemplateModel) args.get(0);
            if (argc == 2) {
                try {
                    startIndex = ((TemplateNumberModel)args.get(1)).getAsNumber().intValue();
                } catch (ClassCastException cce) {
                    throw new TemplateModelException("Expecting number as second argument to ?seq_" + (reverse ? "last_" : "") + "index_of");
                }
            }
            else {
                startIndex = reverse ? sequence.size() - 1 : 0;
            }
            final Environment env = Environment.getCurrentEnvironment();
            final ModelComparator comparator = new ModelComparator(env);
            if (reverse) {
                for (int i = startIndex; i > -1; --i) {
                    if (comparator.modelsEqual(sequence.get(i), compareToThis)) {
                        return new SimpleNumber(i); 
                    }
                }
            }
            else {
                final int s = sequence.size();
                for (int i = startIndex; i < s; ++i) {
                    if (comparator.modelsEqual(sequence.get(i), compareToThis)) {
                        return new SimpleNumber(i); 
                    }
                }
            }
            return new SimpleNumber(-1);
        }
    }

}
