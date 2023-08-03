package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;
import java.util.*;

public class HashLiteral extends Expression {

    private final ArrayList<Expression> keys, values;
    private final int size;

    public HashLiteral(ArrayList<Expression> keys, ArrayList<Expression> values) {
        this.keys = keys;
        this.values = values;
        this.size = keys.size();
        keys.trimToSize();
        values.trimToSize();
        for (Expression exp : keys) {
        	exp.parent = this;
        }
        
        for (Expression exp : values) {
        	exp.parent = this;
        }
    }

    TemplateModel _getAsTemplateModel(Environment env) {
        return new SequenceHash(env);
    }
    
    public List<Expression> getKeys() {
    	return Collections.unmodifiableList(keys);
    }
    
    public List<Expression> getValues() {
    	return Collections.unmodifiableList(values);
    }

    boolean isLiteral() {
        if (constantValue != null) {
            return true;
        }
        for (int i = 0; i < size; i++) {
            Expression key = keys.get(i);
            Expression value = values.get(i);
            if (!key.isLiteral() || !value.isLiteral()) {
                return false;
            }
        }
        return true;
    }


    Expression _deepClone(String name, Expression subst) {
    	ArrayList<Expression> clonedKeys = new ArrayList<Expression>(keys.size());
    	for (Expression exp : keys) {
    		clonedKeys.add(exp.deepClone(name, subst));
    	}
		ArrayList<Expression> clonedValues = new ArrayList<Expression>(values.size());
		for (Expression exp : values) {
			clonedValues.add(exp.deepClone(name, subst));
		}
    	return new HashLiteral(clonedKeys, clonedValues);
    }

    private class SequenceHash implements TemplateHashModelEx {

        private HashMap<String, Object> keyMap  = new HashMap<>(); // maps keys to integer offset
        private TemplateCollectionModel keyCollection, valueCollection; // ordered lists of keys and values

        SequenceHash(Environment env) {
            ArrayList<String> keyList = new ArrayList<String>(size);
            ArrayList<Object> valueList = new ArrayList<>(size);
            for (int i = 0; i< size; i++) {
                Expression keyExp = keys.get(i);
                Expression valExp = values.get(i);
                String key = keyExp.getStringValue(env);
                Object value = valExp.getAsTemplateModel(env);
                assertIsDefined(value, valExp, env);
                keyMap.put(key, value);
                keyList.add(key);
                valueList.add(value);
            }
            keyCollection = new CollectionAndSequence(new SimpleSequence(keyList));
            valueCollection = new CollectionAndSequence(new SimpleSequence(valueList));
        }

        public int size() {
            return size;
        }

        public TemplateCollectionModel keys() {
            return keyCollection;
        }

        public TemplateCollectionModel values() {
            return valueCollection;
        }

        public Object get(String key) {
            return keyMap.get(key);
        }

        public boolean isEmpty() {
            return size == 0;
        }
    }
}
