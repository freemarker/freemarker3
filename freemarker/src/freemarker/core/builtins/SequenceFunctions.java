/*
 * Copyright (c) 2007 The Visigoth Software Society. All rights
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
import freemarker.template.*;
import freemarker.template.utility.StringUtil;

/**
 * Implementations of builtins for standard functions that operate on sequences
 */

public class SequenceFunctions extends BuiltIn {
	
    static final int KEY_TYPE_STRING = 1;
    static final int KEY_TYPE_NUMBER = 2;
    static final int KEY_TYPE_DATE = 3;
	

	public TemplateModel get(TemplateModel target, String builtInName,
			Environment env, BuiltInExpression callingExpression)
			throws TemplateException {
		if (!(target instanceof TemplateSequenceModel)) {
			if (builtInName == "seq_contains" && target instanceof TemplateCollectionModel) {
				// Hacky special case
				return new SequenceContainsFunction(target, env, callingExpression); 
			}
			throw TemplateNode.invalidTypeException(target,
					callingExpression.getTarget(), env, "sequence");
		}
		TemplateSequenceModel sequence = (TemplateSequenceModel) target;
		return getSequenceFunction(sequence, builtInName, env,
				callingExpression);
	}

	private TemplateModel getSequenceFunction(TemplateSequenceModel sequence,
			String builtInName, Environment env,
			BuiltInExpression callingExpression) throws TemplateException {
		if (builtInName == "first") {
			return sequence.size() > 0 ? sequence.get(0) : null;
		}
		if (builtInName == "last") {
			return sequence.size() > 0 ? sequence.get(sequence.size() - 1)
					: null;
		}
		if (builtInName == "reverse") {
			return new ReverseSequence(sequence);
		}
		if (builtInName == "sort") {
			return sort(sequence, null);
		}
		if (builtInName == "sort_by") {
			return new SortByMethod(sequence);
		}
		if (builtInName == "chunk") {
			return new ChunkFunction(sequence);
		}
		if (builtInName == "seq_contains") {
			return new SequenceContainsFunction(sequence, env, callingExpression);
		}
		if (builtInName == "seq_index_of" || builtInName == "seq_last_index_of") {
			return new SequenceIndexOf(sequence, env, callingExpression);
		}
		throw new InternalError("Cannot deal with built-in ?" + builtInName);
	}

	static class ReverseSequence implements TemplateSequenceModel {
		private final TemplateSequenceModel seq;

		ReverseSequence(TemplateSequenceModel seq) {
			this.seq = seq;
		}

		public int size() throws TemplateModelException {
			return seq.size();
		}

		public TemplateModel get(int index) throws TemplateModelException {
			return seq.get(seq.size() - 1 - index);
		}
	}

	static class ChunkFunction implements TemplateMethodModelEx {

		private final TemplateSequenceModel tsm;

		private ChunkFunction(TemplateSequenceModel tsm) {
			this.tsm = tsm;
		}

		public Object exec(List args) throws TemplateModelException {
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
				throws TemplateModelException {
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
				throws TemplateModelException {
			if (chunkIndex >= numberOfChunks) {
				return null;
			}

			return new TemplateSequenceModel() {

				private final int baseIndex = chunkIndex * chunkSize;

				public TemplateModel get(int relIndex)
						throws TemplateModelException {
					int absIndex = baseIndex + relIndex;
					if (absIndex < wrappedTsm.size()) {
						return wrappedTsm.get(absIndex);
					} else {
						return absIndex < numberOfChunks * chunkSize ? fillerItem
								: null;
					}
				}

				public int size() throws TemplateModelException {
					return fillerItem != null
							|| chunkIndex + 1 < numberOfChunks ? chunkSize
							: wrappedTsm.size() - baseIndex;
				}

			};
		}

		public int size() throws TemplateModelException {
			return numberOfChunks;
		}

	}

	static TemplateSequenceModel sort(TemplateSequenceModel seq, String[] keys)
			throws TemplateModelException {
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
                throws TemplateModelException {
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
    
    /*
     * WARNING! This algorithm is duplication of ComparisonExpression.isTrue(...).
     * Thus, if you update this method, then you have to update that too!
     */
    public static boolean modelsEqual(TemplateModel model1, TemplateModel model2,
                                Environment env, Template template)
            throws TemplateModelException {
        int comp = -1;
        if(model1 instanceof TemplateNumberModel && model2 instanceof TemplateNumberModel) {
            Number first = ((TemplateNumberModel) model1).getAsNumber();
            Number second = ((TemplateNumberModel) model2).getAsNumber();
            ArithmeticEngine ae =
                env != null
                    ? env.getArithmeticEngine()
                    : template.getArithmeticEngine();
            try {
                comp = ae.compareNumbers(first, second);
            } catch (TemplateException ex) {
                throw new TemplateModelException(ex);
            }
        }
        else if(model1 instanceof TemplateDateModel && model2 instanceof TemplateDateModel) {
            TemplateDateModel ltdm = (TemplateDateModel)model1;
            TemplateDateModel rtdm = (TemplateDateModel)model2;
            int ltype = ltdm.getDateType();
            int rtype = rtdm.getDateType();
            if(ltype != rtype) {
                throw new TemplateModelException(
                    "Can not compare dates of different type. Left date is of "
                    + TemplateDateModel.TYPE_NAMES.get(ltype)
                    + " type, right date is of "
                    + TemplateDateModel.TYPE_NAMES.get(rtype) + " type.");
            }
            if(ltype == TemplateDateModel.UNKNOWN) {
                throw new TemplateModelException(
                    "Left date is of UNKNOWN type, and can not be compared.");
            }
            if(rtype == TemplateDateModel.UNKNOWN) {
                throw new TemplateModelException(
                    "Right date is of UNKNOWN type, and can not be compared.");
            }
            Date first = ltdm.getAsDate();
            Date second = rtdm.getAsDate();
            comp = first.compareTo(second);
        }
        else if(model1 instanceof TemplateScalarModel && model2 instanceof TemplateScalarModel) {
            String first = ((TemplateScalarModel) model1).getAsString();
            String second = ((TemplateScalarModel) model2).getAsString();
            Collator collator;
            if(env == null) {
                collator = Collator.getInstance(template.getLocale());
            } else {
                collator = env.getCollator();
            }
            comp = collator.compare(first, second);
        }
        else if(model1 instanceof TemplateBooleanModel && model2 instanceof TemplateBooleanModel) {
            boolean first = ((TemplateBooleanModel)model1).getAsBoolean();
            boolean second = ((TemplateBooleanModel)model2).getAsBoolean();
            comp = (first ? 1 : 0) - (second ? 1 : 0);
        }

        return (comp == 0);
    }
    
    static class SequenceContainsFunction implements TemplateMethodModelEx {
    	TemplateSequenceModel sequence;
    	TemplateCollectionModel collection;
    	Environment env;
    	BuiltInExpression callingExpression;
    	SequenceContainsFunction(TemplateModel seqModel, Environment env, BuiltInExpression callingExpression) {
    		this.env = env;
    		this.callingExpression = callingExpression;
    		if (seqModel instanceof TemplateSequenceModel) {
    			sequence = (TemplateSequenceModel) seqModel;
    		}
    		else if (seqModel instanceof TemplateCollectionModel) {
    			collection = (TemplateCollectionModel) seqModel;
    		}
    	}
    	
    	public TemplateModel exec(List args) throws TemplateModelException {
    		if (args.size() != 1) {
    			throw new TemplateModelException("Expecting exactly one argument for ?seq_contains(...)");
    		}
    		TemplateModel compareToThis = (TemplateModel) args.get(0);
    		if (collection != null) {
    			TemplateModelIterator tmi = collection.iterator();
    			while (tmi.hasNext()) {
    				if (modelsEqual(tmi.next(), compareToThis, env, callingExpression.getTemplate())) {
    					return TemplateBooleanModel.TRUE;
    				}
    			}
   	   			return TemplateBooleanModel.FALSE;
    		}
    		else {
    			for (int i=0; i<sequence.size(); i++) {
    				if (modelsEqual(sequence.get(i), compareToThis, env, callingExpression.getTemplate())) {
    					return TemplateBooleanModel.TRUE;
    				}
    			}
       			return TemplateBooleanModel.FALSE;
    		}
   		}
    }
    
    static class SequenceIndexOf implements TemplateMethodModelEx {
    	
    	TemplateSequenceModel sequence;
    	BuiltInExpression callingExpression;
    	Environment env;
    	
    	SequenceIndexOf(TemplateSequenceModel sequence, Environment env, BuiltInExpression callingExpression) {
    		this.sequence = sequence;
    		this.env = env;
    		this.callingExpression = callingExpression;
    	}
    	
    	public TemplateModel exec(List args) throws TemplateModelException {
    		int argc = args.size();
    		int startIndex = 0;
    		if (argc != 1 && argc != 2) {
    			throw new TemplateModelException("Expecting one or two arguments for ?" + callingExpression.getName());
    		}
    		TemplateModel compareToThis = (TemplateModel) args.get(0);
    		if (argc == 2) {
    			try {
    				startIndex = ((TemplateNumberModel)args.get(1)).getAsNumber().intValue();
    			} catch (ClassCastException cce) {
    				throw new TemplateModelException("Expecting number as second argument to ?" + callingExpression.getName());
    			}
    		}
    		boolean reverse = callingExpression.getName() == "seq_last_index_of";
    		if (reverse) {
    			sequence = new ReverseSequence(sequence);
    			if (argc == 2) {
    				startIndex = sequence.size() - 1 - startIndex;
    			}
    		}
    		for (int i=startIndex; i<sequence.size(); i++) {
    			if (modelsEqual(sequence.get(i), compareToThis, env, callingExpression.getTemplate())) {
    				int result = reverse ? sequence.size() - i -1 : i;
    				return new SimpleNumber(result); 
    			}
    		}
    		return new SimpleNumber(-1);
    	}
    }
    
}
