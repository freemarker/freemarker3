package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.ext.beans.StringModel;
import static freemarker.ext.beans.ObjectWrapper.*;
import java.util.*;

/**
 * An operator for the + operator. Note that this is treated
 * separately from the other 4 arithmetic operators,
 * since + is overloaded to mean concatenation of strings or arrays.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
public class AddConcatExpression extends Expression {

    public AddConcatExpression() {}

    public AddConcatExpression(Expression left, Expression right) {
        add(left);
        add(right);
    }
    
    public Expression getLeft() {
    	return (Expression) get(0);
    }
    
    public Expression getRight() {
    	return (Expression) get(1);
    }

    public Object evaluate(Environment env)
            throws TemplateException
    {
        Object leftModel = getLeft().evaluate(env);
        Object rightModel = getRight().evaluate(env);
        if (leftModel instanceof Number && rightModel instanceof Number) {
            Number first = (Number) leftModel;
            Number second = (Number) rightModel;
            ArithmeticEngine ae =
                env != null
                    ? env.getArithmeticEngine()
                    : getTemplate().getArithmeticEngine();
            return ae.add(first, second);
        }
        if (isNumber(leftModel) && isNumber(rightModel))
        {
            Number first = asNumber(leftModel);
            Number second = asNumber(rightModel);
            ArithmeticEngine ae =
                env != null
                    ? env.getArithmeticEngine()
                    : getTemplate().getArithmeticEngine();
            return ae.add(first, second);
        }
        else if (isList(leftModel) && isList(rightModel)) 
        {/*
            List merged = new ArrayList(asList(leftModel));
            merged.addAll(asList(rightModel));
            return merged;*/
            return new ConcatenatedSequence((TemplateSequenceModel)leftModel, (TemplateSequenceModel)rightModel);
        }
        else if (isDisplayableAsString(leftModel) && isDisplayableAsString(rightModel)) {
            String s1 = getStringValue(leftModel, getLeft(), env);
            if(s1 == null) s1 = "null";
            String s2 = getStringValue(rightModel, getRight(), env);
            if(s2 == null) s2 = "null";
            return new StringModel(s1.concat(s2));
        }
        else if (leftModel instanceof TemplateHashModel && rightModel instanceof TemplateHashModel) {
            if (leftModel instanceof TemplateHashModelEx && rightModel instanceof TemplateHashModelEx) {
                TemplateHashModelEx leftModelEx = (TemplateHashModelEx)leftModel;
                TemplateHashModelEx rightModelEx = (TemplateHashModelEx)rightModel;
                if (leftModelEx.size() == 0) {
                    return rightModelEx;
                } else if (rightModelEx.size() == 0) {
                    return leftModelEx;
                } else {
                    return new ConcatenatedHashEx(leftModelEx, rightModelEx);
                }
            } else {
                return new ConcatenatedHash((TemplateHashModel)leftModel,
                                            (TemplateHashModel)rightModel);
            }
        }
        String msg = this.getStartLocation() + ": Cannot add or concatenate";
        throw new TemplateException(msg, env);
    }

    public Expression _deepClone(String name, Expression subst) {
    	return new AddConcatExpression(getLeft().deepClone(name, subst), getRight().deepClone(name, subst));
    }

    private static final class ConcatenatedSequence implements TemplateSequenceModel
    {
        private final TemplateSequenceModel left;
        private final TemplateSequenceModel right;

        ConcatenatedSequence(TemplateSequenceModel left, TemplateSequenceModel right)
        {
            this.left = left;
            this.right = right;
        }

        public int size() {
            return left.size() + right.size();
        }

        public Object get(int i) {
            int ls = left.size();
            return i < ls ? left.get(i) : right.get(i - ls);
        }
    }

    private static class ConcatenatedHash implements TemplateHashModel {
        final TemplateHashModel left;
        final TemplateHashModel right;

        ConcatenatedHash(TemplateHashModel left, TemplateHashModel right)
        {
            this.left = left;
            this.right = right;
        }
        
        public Object get(String key)
        {
            Object model = right.get(key);
            return (model != null) ? model : left.get(key);
        }

        public boolean isEmpty()
        {
            return left.isEmpty() && right.isEmpty();
        }
    }

    private static final class ConcatenatedHashEx extends ConcatenatedHash implements TemplateHashModelEx
    {
        private Iterable<String> keys;
        private Iterable<Object> values;
        private int size;

        ConcatenatedHashEx(TemplateHashModelEx left, TemplateHashModelEx right)
        {
            super(left, right);
        }
        
        public int size() throws TemplateModelException
        {
            initKeys();
            return size;
        }

        public Iterable keys()
        {
            initKeys();
            return keys;
        }

        public Iterable values()
        {
            initValues();
            return values;
        }

        private void initKeys()
        {
            if (keys == null) {
                HashSet<String> keySet = new HashSet<String>();
                List<String> keySeq = new ArrayList<>();
                addKeys(keySet, keySeq, (TemplateHashModelEx)this.left);
                addKeys(keySet, keySeq, (TemplateHashModelEx)this.right);
                size = keySet.size();
                keys = keySeq;
            }
        }

        private static void addKeys(Set<String> set, List<String> keySeq, TemplateHashModelEx hash)
        throws TemplateModelException
        {
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
