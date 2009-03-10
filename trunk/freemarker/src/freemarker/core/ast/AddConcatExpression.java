/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;
import java.util.*;

/**
 * An operator for the + operator. Note that this is treated
 * separately from the other 4 arithmetic operators,
 * since + is overloaded to mean concatenation of strings or arrays.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
public class AddConcatExpression extends Expression {

    public final Expression left;
    public final Expression right;

    public AddConcatExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
        left.parent = this;
        right.parent = this;
    }

    TemplateModel _getAsTemplateModel(Environment env)
            throws TemplateException
    {
        TemplateModel leftModel = left.getAsTemplateModel(env);
        TemplateModel rightModel = right.getAsTemplateModel(env);
        if (leftModel instanceof TemplateNumberModel && rightModel instanceof TemplateNumberModel)
        {
            Number first = EvaluationUtil.getNumber((TemplateNumberModel) leftModel, left, env);
            Number second = EvaluationUtil.getNumber((TemplateNumberModel) rightModel, right, env);
            ArithmeticEngine ae =
                env != null
                    ? env.getArithmeticEngine()
                    : getTemplate().getArithmeticEngine();
            return new SimpleNumber(ae.add(first, second));
        }
        else if(leftModel instanceof TemplateSequenceModel && rightModel instanceof TemplateSequenceModel)
        {
            return new ConcatenatedSequence((TemplateSequenceModel)leftModel, (TemplateSequenceModel)rightModel);
        }
        else if (isDisplayableAsString(leftModel) && isDisplayableAsString(rightModel)) {
            String s1 = getStringValue(leftModel, left, env);
            if(s1 == null) s1 = "null";
            String s2 = getStringValue(rightModel, right, env);
            if(s2 == null) s2 = "null";
            return new SimpleScalar(s1.concat(s2));
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

    boolean isLiteral() {
    	if ((right instanceof StringLiteral && !(left instanceof StringLiteral))
    		|| (!(right instanceof StringLiteral) && left instanceof StringLiteral))
    		return false; // REVISIT (This is hacky, but the problem is that
    	                  // we can't do a parse-time optimization of, say,
    	                  // ${"The answer is: " + 1.1}
    	                  // since the display of the decimal number depends i18n
    	                  // considerations only known at render-time. (JR))
        return constantValue != null || (left.isLiteral() && right.isLiteral());
    }

    Expression _deepClone(String name, Expression subst) {
    	return new AddConcatExpression(left.deepClone(name, subst), right.deepClone(name, subst));
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

        public int size()
        throws
            TemplateModelException
        {
            return left.size() + right.size();
        }

        public TemplateModel get(int i)
        throws
            TemplateModelException
        {
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
        
        public TemplateModel get(String key)
        throws TemplateModelException
        {
            TemplateModel model = right.get(key);
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
                SimpleSequence keySeq = new SimpleSequence(32);
                addKeys(keySet, keySeq, (TemplateHashModelEx)this.left);
                addKeys(keySet, keySeq, (TemplateHashModelEx)this.right);
                size = keySet.size();
                keys = new CollectionAndSequence(keySeq);
            }
        }

        private static void addKeys(Set<String> set, SimpleSequence keySeq, TemplateHashModelEx hash)
        throws TemplateModelException
        {
            TemplateModelIterator it = hash.keys().iterator();
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
                SimpleSequence seq = new SimpleSequence(size());
                // Note: size() invokes initKeys() if needed.
            
                int ln = keys.size();
                for (int i  = 0; i < ln; i++) {
                    seq.add(get(((TemplateScalarModel)keys.get(i)).getAsString()));
                }
                values = new CollectionAndSequence(seq);
            }
        }
    }
}