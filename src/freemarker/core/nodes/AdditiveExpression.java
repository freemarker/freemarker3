package freemarker.core.nodes;

import freemarker.core.ArithmeticEngine;
import freemarker.core.Environment;
import freemarker.core.nodes.generated.Expression;
import freemarker.core.nodes.generated.TemplateNode;
import freemarker.core.variables.*;
import freemarker.template.TemplateException;
import java.util.*;

import static freemarker.core.parser.Token.TokenType.*;
import static freemarker.core.variables.Wrap.*;


//THIS NEEDS TO BE SIMPLIFIED!
public class AdditiveExpression extends TemplateNode implements Expression {

    public boolean isPlus() {
        return get(1).getType() == PLUS;
    }

    public Expression getLeft() {
        return (Expression) get(0);
    }

    public Expression getRight() {
        return (Expression) get(2);
    }

    public Object evaluate(Environment env) {
        Object left = getLeft().evaluate(env);
        Object right = getRight().evaluate(env);
        ArithmeticEngine ae = env != null ? env.getArithmeticEngine() : getTemplate().getArithmeticEngine();
        if (!isPlus()) {
            Number first = Wrap.getNumber(left, getLeft(), env);
            Number second = Wrap.getNumber(right, getRight(), env);
            return ae.subtract(first, second);
        }
        boolean bothAreNumbers = isNumber(left) && isNumber(right);
        if (bothAreNumbers) {
            Number first = Wrap.getNumber(left, getLeft(), env);
            Number second = Wrap.getNumber(right, getRight(), env);
            return ae.add(first, second);
        }
        if (isList(left) && isList(right)) {
            List leftList = asList(left);
            List rightList = asList(right);
            List result = new ArrayList();
            result.addAll(leftList);
            result.addAll(rightList);
            return result;
        }
        if (isDisplayableAsString(left) && isDisplayableAsString(right)) {
            return asString(left) + asString(right);
        }
        if (left instanceof WrappedHash && right instanceof WrappedHash) {
            if (left instanceof WrappedHash && right instanceof WrappedHash) {
                WrappedHash leftModelEx = (WrappedHash) left;
                WrappedHash rightModelEx = (WrappedHash) right;
                if (leftModelEx.size() == 0) {
                    return rightModelEx;
                } else if (rightModelEx.size() == 0) {
                    return leftModelEx;
                } else {
                    return new ConcatenatedHashEx(leftModelEx, rightModelEx);
                }
            } else {
                return new ConcatenatedHash((WrappedHash) left, (WrappedHash) right);
            }
        }
        if (isMap(left) && isMap(right)) {
            Map leftMap = (Map) unwrap(left);
            Map rightMap = (Map) unwrap(right);
            Map result = new LinkedHashMap(leftMap);
            for (Object key : rightMap.keySet()) {
                result.put(key, rightMap.get(key));
            }
            return result;
        }
        String msg = this.getLocation() + ": Cannot add or concatenate";
        throw new TemplateException(msg, env);
    }

    public Expression _deepClone(String name, Expression subst) {
        AdditiveExpression result = new AdditiveExpression();
        result.add(getLeft().deepClone(name, subst));
        result.add(get(1));
        result.add(getRight().deepClone(name, subst));
        return result;
    }

    private static class ConcatenatedHash implements WrappedHash {
        final WrappedHash left;
        final WrappedHash right;

        ConcatenatedHash(WrappedHash left, WrappedHash right) {
            this.left = left;
            this.right = right;
        }

        public Object get(String key) {
            Object model = right.get(key);
            return (model != null) ? model : left.get(key);
        }

        public boolean isEmpty() {
            return left.isEmpty() && right.isEmpty();
        }
    }


    private static final class ConcatenatedHashEx extends ConcatenatedHash {
        private Iterable<String> keys;
        private Iterable<Object> values;
        private int size;

        ConcatenatedHashEx(WrappedHash left, WrappedHash right) {
            super(left, right);
        }

        public int size() throws EvaluationException {
            initKeys();
            return size;
        }

        public Iterable keys() {
            initKeys();
            return keys;
        }

        public Iterable values() {
            initValues();
            return values;
        }

        private void initKeys() {
            if (keys == null) {
                HashSet<String> keySet = new HashSet<String>();
                List<String> keySeq = new ArrayList<>();
                addKeys(keySet, keySeq, (WrappedHash) this.left);
                addKeys(keySet, keySeq, (WrappedHash) this.right);
                size = keySet.size();
                keys = keySeq;
            }
        }

        private static void addKeys(Set<String> set, List<String> keySeq, WrappedHash hash) throws EvaluationException {
            Iterator<?> it = hash.keys().iterator();
            while (it.hasNext()) {
                String s = asString(it.next());
                if (set.add(s)) {
                    // The first occurence of the key decides the index;
                    // this is consisten with stuff like java.util.LinkedHashSet.
                    keySeq.add(s);
                }
            }
        }

        private void initValues() {
            if (values == null) {
                List<Object> seq = new ArrayList<>();
                for (String key : keys) {
                    seq.add(get(key));
                }
                values = seq;
            }
        }

    }

}


