package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.ext.beans.StringModel;
import freemarker.ext.beans.NumberModel;
import freemarker.ext.beans.ListModel;
import java.util.*;

/**
 * An operator for the + operator. Note that this is treated
 * separately from the other 4 arithmetic operators,
 * since + is overloaded to mean concatenation of strings or arrays.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
public class AddConcatExpression extends Expression {

//    private Expression left;
//    private Expression right;
    public AddConcatExpression() {}

    public AddConcatExpression(Expression left, Expression right) {
//        this.left = left;
//        this.right = right;
        add(left);
        add(right);
//        left.setParent(this);
//        right.setParent(this);
    }
    
    public Expression getLeft() {
    	return (Expression) get(0);
    }
    
    public Expression getRight() {
    	return (Expression) get(1);
    }

    public Object getAsTemplateModel(Environment env)
            throws TemplateException
    {
        Object leftModel = getLeft().getAsTemplateModel(env);
        Object rightModel = getRight().getAsTemplateModel(env);
        if (leftModel instanceof Number && rightModel instanceof Number) {
            Number first = (Number) leftModel;
            Number second = (Number) rightModel;
            ArithmeticEngine ae =
                env != null
                    ? env.getArithmeticEngine()
                    : getTemplate().getArithmeticEngine();
            return new NumberModel(ae.add(first, second));
        }
        if (leftModel instanceof TemplateNumberModel && rightModel instanceof TemplateNumberModel)
        {
            Number first = EvaluationUtil.getNumber((TemplateNumberModel) leftModel, getLeft(), env);
            Number second = EvaluationUtil.getNumber((TemplateNumberModel) rightModel, getRight(), env);
            ArithmeticEngine ae =
                env != null
                    ? env.getArithmeticEngine()
                    : getTemplate().getArithmeticEngine();
            return new NumberModel(ae.add(first, second));
        }
        else if(leftModel instanceof TemplateSequenceModel && rightModel instanceof TemplateSequenceModel)
        {
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

    private static final class ConcatenatedSequence
    implements
        TemplateSequenceModel
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

    private static class ConcatenatedHash
    implements TemplateHashModel
    {
        protected final TemplateHashModel left;
        protected final TemplateHashModel right;

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
        throws TemplateModelException
        {
            return left.isEmpty() && right.isEmpty();
        }
    }

    private static final class ConcatenatedHashEx
    extends ConcatenatedHash
    implements TemplateHashModelEx
    {
        private CollectionAndSequence keys;
        private CollectionAndSequence values;
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

        public TemplateCollectionModel keys()
        throws TemplateModelException
        {
            initKeys();
            return keys;
        }

        public TemplateCollectionModel values()
        throws TemplateModelException
        {
            initValues();
            return values;
        }

        private void initKeys()
        throws TemplateModelException
        {
            if (keys == null) {
                HashSet<String> keySet = new HashSet<String>();
                ListModel keySeq = new ListModel();
                addKeys(keySet, keySeq, (TemplateHashModelEx)this.left);
                addKeys(keySet, keySeq, (TemplateHashModelEx)this.right);
                size = keySet.size();
                keys = new CollectionAndSequence((TemplateSequenceModel)keySeq);
            }
        }

        private static void addKeys(Set<String> set, ListModel keySeq, TemplateHashModelEx hash)
        throws TemplateModelException
        {
            Iterator<Object> it = hash.keys().iterator();
            while (it.hasNext()) {
                TemplateScalarModel tsm = (TemplateScalarModel)it.next();
                if (set.add(tsm.getAsString())) {
                    // The first occurence of the key decides the index;
                    // this is consisten with stuff like java.util.LinkedHashSet.
                    keySeq.add(tsm);
                }
            }
        }        

        private void initValues()
        throws TemplateModelException
        {
            if (values == null) {
                ListModel seq = new ListModel();
                // Note: size() invokes initKeys() if needed.
            
                int ln = keys.size();
                for (int i  = 0; i < ln; i++) {
                    seq.add(get(((TemplateScalarModel)keys.get(i)).getAsString()));
                }
                values = new CollectionAndSequence((TemplateSequenceModel)seq);
            }
        }
    }
}
