/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

    TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
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

        private HashMap<String, TemplateModel> keyMap  = new HashMap<String, TemplateModel>(); // maps keys to integer offset
        private TemplateCollectionModel keyCollection, valueCollection; // ordered lists of keys and values

        SequenceHash(Environment env) throws TemplateException {
            ArrayList<String> keyList = new ArrayList<String>(size);
            ArrayList<TemplateModel> valueList = new ArrayList<TemplateModel>(size);
            for (int i = 0; i< size; i++) {
                Expression keyExp = keys.get(i);
                Expression valExp = values.get(i);
                String key = keyExp.getStringValue(env);
                TemplateModel value = valExp.getAsTemplateModel(env);
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

        public TemplateModel get(String key) {
            return keyMap.get(key);
        }

        public boolean isEmpty() {
            return size == 0;
        }
    }
}
