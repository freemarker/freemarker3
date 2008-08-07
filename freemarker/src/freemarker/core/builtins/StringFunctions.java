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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.NonNumericalException;
import freemarker.core.ast.TemplateNode;
import freemarker.template.*;
import freemarker.template.utility.StringUtil;

/**
 * Implementations of ?substring and other 
 * standard functions that operate on strings
 */

public class StringFunctions extends BuiltIn {
	
    static private HashMap<String, Pattern> patternLookup = new HashMap<String, Pattern>();
    static private LinkedList<String> patterns = new LinkedList<String>();
    static private final int PATTERN_CACHE_SIZE=100;
    
    static Pattern getPattern(String patternString, String flagString) throws TemplateModelException {
        int flags = 0;
        String patternKey = patternString + (char) 0 + flagString;
        Pattern result = patternLookup.get(patternKey);
        if (result != null) {
            return result;
        }
        if (flagString == null || flagString.length() == 0) {
            try {
                result = Pattern.compile(patternString);
            } catch (PatternSyntaxException e) {
                throw new TemplateModelException(e);
            }
        }
        else {
            if (flagString.indexOf('i') >=0) {
                flags = flags | Pattern.CASE_INSENSITIVE;
            }
            if (flagString.indexOf('m') >=0) {
                flags = flags | Pattern.MULTILINE;
            }
            if (flagString.indexOf('c') >=0) {
                flags = flags | Pattern.COMMENTS;
            }
            if (flagString.indexOf('s') >=0) {
                flags = flags | Pattern.DOTALL;
            }
            try {
                result = Pattern.compile(patternString, flags);
            } catch (PatternSyntaxException e) {
                throw new TemplateModelException(e);
            }
        }
        synchronized (patterns) {
            patterns.add(patternKey);
            patternLookup.put(patternKey, result);
            if (patterns.size() > PATTERN_CACHE_SIZE) {
                Object first = patterns.removeFirst();
                patterns.remove(first);
            }
        }
        return result;
    }
    
	
	
	public TemplateModel get(TemplateModel target, String builtInName, Environment env, BuiltInExpression callingExpression) throws TemplateException {
		if (builtInName == "number" && target instanceof TemplateNumberModel) return target; 
		if (builtInName == "groups") {
            if (target instanceof RegexMatchModel) {
                return ((RegexMatchModel) target).getGroups();
            }
            if (target instanceof RegexMatchModel.Match) {
                return ((RegexMatchModel.Match) target).subs;
            }
            else {
    			throw TemplateNode.invalidTypeException(target, callingExpression.getTarget(), env, "regular expression matcher");
            }
		}
		else try {
			String string = ((TemplateScalarModel) target).getAsString();
			return getStringFunction(string, builtInName, env, callingExpression);
		} catch (ClassCastException cce) {
			throw TemplateNode.invalidTypeException(target, callingExpression.getTarget(), env, "string");
		} catch (NullPointerException npe) {
			throw new InvalidReferenceException("String is undefined", env);
		}
	}
	
	private TemplateModel getStringFunction(final String string, final String builtInName, 
			final Environment env, final BuiltInExpression callingExpression) throws TemplateException {
		if (builtInName == "length") {
			return new SimpleNumber(Integer.valueOf(string.length())); 
		}
		if (builtInName == "number") {
            try {
                return new SimpleNumber(env.getArithmeticEngine().toNumber(string));
            }
            catch(NumberFormatException nfe) {
                String mess = "Error: " + callingExpression.getStartLocation()
                             + "\nExpecting a number here, found: " + string;
                throw new NonNumericalException(mess, env);
            }
		}
		if (builtInName == "starts_with" || builtInName == "ends_with") {
			return new StartsOrEndsWithMethod(string, callingExpression);
		}
		if (builtInName == "substring") {
			return new SubstringMethod(string, callingExpression);
		}
		if (builtInName == "replace") {
			return new ReplaceMethod(string);
		}
		if (builtInName == "split") {
			return new SplitMethod(string);
		}
		if (builtInName == "matches") {
			return new MatcherBuilder(string);
		}
		if (builtInName == "index_of" || builtInName == "last_index_of")  {
			return new IndexOfMethod(string, callingExpression);
		}
		if (builtInName == "contains") {
			return new ContainsMethod(string);
		}
		if (builtInName == "left_pad") {
			return new LeftPadMethod(string);
		}
		if (builtInName == "right_pad") {
			return new RightPadMethod(string);
		}
		if (builtInName == "word_list") {
			StringTokenizer st = new StringTokenizer(string);
			SimpleSequence result = new SimpleSequence();
			while (st.hasMoreTokens()) {
				result.add(st.nextToken());
			}
			return result;
		}
		if (builtInName == "url") {
			return new urlBIResult(string, env);
		}
		throw new InternalError("Cannot deal with built-in ?" + builtInName);
	}
	
	static class ReplaceMethod implements TemplateMethodModel {
		String string;
		
		ReplaceMethod(String string) {
			this.string = string;
		}
		
        public Object exec(List args) throws TemplateModelException {
            int numArgs = args.size();
            if (numArgs < 2 || numArgs >3 ) {
                throw new TemplateModelException(
                        "?replace(...) needs 2 or 3 arguments.");
            }
            String first = (String) args.get(0);
            String second = (String) args.get(1);
            String flags = numArgs >2 ? (String) args.get(2) : "";
            boolean caseInsensitive = flags.indexOf('i') >=0;
            boolean useRegexp = flags.indexOf('r') >=0;
            boolean firstOnly = flags.indexOf('f') >=0;
            String result = null;
            if (!useRegexp) {
                result = StringUtil.replace(string, first, second, caseInsensitive, firstOnly);
            } else {
                Pattern pattern = getPattern(first, flags);
                Matcher matcher = pattern.matcher(string);
                result = firstOnly ? matcher.replaceFirst(second) : matcher.replaceAll(second);
            } 
            return new SimpleScalar(result);
        }
	}
	
	static class SubstringMethod implements TemplateMethodModelEx {
		
		String string;
		BuiltInExpression callingExpression;
		
		SubstringMethod(String string, BuiltInExpression callingExpression) {
			this.string = string;
			this.callingExpression = callingExpression;
		}
		
		public Object exec(java.util.List args) throws TemplateModelException {
			int argCount = args.size(), left=0, right=0;
			if (argCount != 1 && argCount != 2) {
				throw new TemplateModelException("Error: +getStartLocation() + \nExpecting 1 or 2 numerical arguments here");
			}
				try {
					TemplateNumberModel tnm = (TemplateNumberModel) args.get(0);
					left = tnm.getAsNumber().intValue();
					if (argCount == 2) {
						tnm = (TemplateNumberModel) args.get(1);
						right = tnm.getAsNumber().intValue();
					}
				} catch (ClassCastException cce) {
					String mess = "Error: " + callingExpression.getStartLocation() + "\nExpecting numerical argument here";
					throw new TemplateModelException(mess);
				}
			if (argCount == 1) {
				return new SimpleScalar(string.substring(left));
			} 
			return new SimpleScalar(string.substring(left, right));
		}
	}
	
	
    static class SplitMethod implements TemplateMethodModel {
        private String string;

        SplitMethod(String string) {
            this.string = string;
        }

        public Object exec(List args) throws TemplateModelException {
            int numArgs = args.size();
            if (numArgs < 1 || numArgs >2 ) {
                throw new TemplateModelException(
                        "?replace(...) needs 1 or 2 arguments.");
            }
            String splitString = (String) args.get(0);
            String flags = numArgs >1 ? (String) args.get(1) : "";
            boolean caseInsensitive = flags.indexOf('i') >=0;
            boolean useRegexp = flags.indexOf('r') >=0;
            String[] result = null;
            if (!useRegexp) {
                result = StringUtil.split(string, splitString, caseInsensitive);
            } else {
                Pattern pattern = getPattern(splitString, flags);
                result = pattern.split(string);
            } 
            return ObjectWrapper.DEFAULT_WRAPPER.wrap(result);
        }
    }
    
    static class MatcherBuilder implements TemplateMethodModel {
        
        String matchString;
        
        MatcherBuilder(String matchString) {
            this.matchString = matchString;
        }
        
        public Object exec(List args) throws TemplateModelException {
            int numArgs = args.size();
            if (numArgs == 0) {
                throw new TemplateModelException("Expecting at least one argument");
            }
            if (numArgs > 2) {
                throw new TemplateModelException("Expecting at most two argumnets");
            }
            String patternString = (String) args.get(0);
            String flagString = (numArgs >1) ? (String) args.get(1) : "";
            Pattern pattern = getPattern(patternString, flagString);
            Matcher matcher = pattern.matcher(matchString);
            return new RegexMatchModel(matcher, matchString);
        }
    }
    
    
    static class RegexMatchModel 
    implements TemplateBooleanModel, TemplateCollectionModel, TemplateSequenceModel {
        Matcher matcher;
        String input;
        final boolean matches;
        TemplateSequenceModel groups;
        private ArrayList<TemplateModel> data;
        
        RegexMatchModel(Matcher matcher, String input) {
            this.matcher = matcher;
            this.input = input;
            this.matches = matcher.matches();
        }
        
        public boolean getAsBoolean() {
            return matches;
        }
        
        public TemplateModel get(int i) throws TemplateModelException {
            if (data == null) initSequence();
            return data.get(i);
        }
        
        public int size() throws TemplateModelException {
            if (data == null) initSequence();
            return data.size();
        }
        
        private void initSequence() throws TemplateModelException {
            data = new ArrayList<TemplateModel>();
            TemplateModelIterator it = iterator();
            while (it.hasNext()) {
                data.add(it.next());
            }
        }
        
        public TemplateModel getGroups() {
           if (groups == null) {
                groups = new TemplateSequenceModel() {
                    public int size() throws TemplateModelException {
                        try {
                            return matcher.groupCount() + 1;
                        }
                        catch (Exception e) {
                            throw new TemplateModelException(e);
                        }
                    }
                    public TemplateModel get(int i) throws TemplateModelException {
                        try {
                            return new SimpleScalar(matcher.group(i));
                        }
                        catch (Exception e) {
                            throw new TemplateModelException(e);
                        }
                    }
                };
            }
            return groups;
        }
        
        public TemplateModelIterator iterator() {
            matcher.reset();
            return new TemplateModelIterator() {
                boolean hasFindInfo = matcher.find();
                
                public boolean hasNext() {
                    return hasFindInfo;
                }
                
                public TemplateModel next() throws TemplateModelException {
                    if (!hasNext()) throw new TemplateModelException("No more matches");
                    Match result = new Match();
                    hasFindInfo = matcher.find();
                    return result;
                }
            };
        }
        
        class Match implements TemplateScalarModel {
            String match;
            SimpleSequence subs = new SimpleSequence();
            Match() {
                match = input.substring(matcher.start(), matcher.end());
                for (int i=0; i< matcher.groupCount() + 1; i++) {
                    subs.add(matcher.group(i));
                }
            }
            public String getAsString() {
                return match;
            }
        }
    }
    
    static class LeftPadMethod implements TemplateMethodModelEx {
        private String string;

        LeftPadMethod(String s) {
            this.string = s;
        }

        public Object exec(List args) throws TemplateModelException {
            Object obj;

            int ln  = args.size();
            if (ln == 0) {
                throw new TemplateModelException(
                        "?left_pad(...) expects at least 1 argument.");
            }
            if (ln > 2) {
                throw new TemplateModelException(
                        "?left_pad(...) expects at most 2 arguments.");
            }

            obj = args.get(0);
            if (!(obj instanceof TemplateNumberModel)) {
                throw new TemplateModelException(
                        "?left_pad(...) expects a number as "
                        + "its 1st argument.");
            }
            int width = ((TemplateNumberModel) obj).getAsNumber().intValue();

            if (ln > 1) {
                obj = args.get(1);
                if (!(obj instanceof TemplateScalarModel)) {
                    throw new TemplateModelException(
                            "?left_pad(...) expects a string as "
                            + "its 2nd argument.");
                }
                String filling = ((TemplateScalarModel) obj).getAsString();
                try {
                    return new SimpleScalar(StringUtil.leftPad(string, width, filling));
                } catch (IllegalArgumentException e) {
                    if (filling.length() == 0) {
                        throw new TemplateModelException(
                                "The 2nd argument of ?left_pad(...) "
                                + "can't be a 0 length string.");
                    } else {
                        throw new TemplateModelException(
                                "Error while executing the ?left_pad(...) "
                                + "built-in.", e);
                    }
                }
            } else {
                return new SimpleScalar(StringUtil.leftPad(string, width));
            }
        }
    }
    
    static class RightPadMethod implements TemplateMethodModelEx {
        private String string;

        private RightPadMethod(String string) {
        	this.string = string;
        }

        public Object exec(List args) throws TemplateModelException {
            Object obj;

            int ln  = args.size();
            if (ln == 0) {
                throw new TemplateModelException(
                        "?right_pad(...) expects at least 1 argument.");
            }
            if (ln > 2) {
                throw new TemplateModelException(
                        "?right_pad(...) expects at most 2 arguments.");
            }

            obj = args.get(0);
            if (!(obj instanceof TemplateNumberModel)) {
                throw new TemplateModelException(
                        "?right_pad(...) expects a number as "
                        + "its 1st argument.");
            }
            int width = ((TemplateNumberModel) obj).getAsNumber().intValue();

            if (ln > 1) {
                obj = args.get(1);
                if (!(obj instanceof TemplateScalarModel)) {
                    throw new TemplateModelException(
                            "?right_pad(...) expects a string as "
                            + "its 2nd argument.");
                }
                String filling = ((TemplateScalarModel) obj).getAsString();
                try {
                    return new SimpleScalar(StringUtil.rightPad(string, width, filling));
                } catch (IllegalArgumentException e) {
                    if (filling.length() == 0) {
                        throw new TemplateModelException(
                                "The 2nd argument of ?right_pad(...) "
                                + "can't be a 0 length string.");
                    } else {
                        throw new TemplateModelException(
                                "Error while executing the ?right_pad(...) "
                                + "built-in.", e);
                    }
                }
            } else {
                return new SimpleScalar(StringUtil.rightPad(string, width));
            }
        }
    }
    
    static class urlBIResult implements TemplateScalarModel, TemplateMethodModel {

    	private final String target;
    	private final Environment env;
    	private String cachedResult;

    	private urlBIResult(String target, Environment env) {
    		this.target = target;
    		this.env = env;
    	}

    	public String getAsString() throws TemplateModelException {
    		if (cachedResult == null) {
    			String cs = env.getEffectiveURLEscapingCharset();
    			if (cs == null) {
    				throw new TemplateModelException(
    				"To do URL encoding, the framework that encloses "
                    + "FreeMarker must specify the output encoding "
                    + "or the URL encoding charset, so ask the "
                    + "programmers to fix it. Or, as a last chance, "
                    + "you can set the url_encoding_charset setting in "
                    + "the template, e.g. "
                    + "<#setting url_escaping_charset='ISO-8859-1'>, or "
                    + "give the charset explicitly to the buit-in, e.g. "
                    + "foo?url('ISO-8859-1').");
    			}
    			try {
    				cachedResult = StringUtil.URLEnc(target, cs);
    			} catch (UnsupportedEncodingException e) {
    				throw new TemplateModelException(
    						"Failed to execute URL encoding.", e);
    			}
    		}
    		return cachedResult;
    	}

    	public Object exec(List args) throws TemplateModelException {
    		if (args.size() != 1) {
    			throw new TemplateModelException("The \"url\" built-in "
                + "needs exactly 1 parameter, the charset.");
    		}	
    		try {
    			return new SimpleScalar(
                StringUtil.URLEnc(target, (String) args.get(0)));
    		} catch (UnsupportedEncodingException e) {
    			throw new TemplateModelException(
                "Failed to execute URL encoding.", e);
    		}
    	}
    }
    
    static class StartsOrEndsWithMethod implements TemplateMethodModelEx {
        private String string;
        BuiltInExpression callingExpression;
        boolean isEndsWith;

        private StartsOrEndsWithMethod(String string, BuiltInExpression callingExpression) {
            this.string = string;
            this.callingExpression = callingExpression;
            isEndsWith = callingExpression.getName() == "ends_with";
        }

        public Object exec(List args) throws TemplateModelException {
            String sub;

            if (args.size() != 1) {
                throw new TemplateModelException(
                        "?" + callingExpression.getName()+ "(...) expects exactly 1 argument.");
            }

            Object obj = args.get(0);
            if (!(obj instanceof TemplateScalarModel)) {
                throw new TemplateModelException(
                        "?"+ callingExpression.getName() + "(...) expects a string argument");
            }
            sub = ((TemplateScalarModel) obj).getAsString();
            
            boolean result = isEndsWith ? string.endsWith(sub) : string.startsWith(sub);
            return result ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
        }
    }
    
    static class IndexOfMethod implements TemplateMethodModelEx {
        private String s;
        BuiltInExpression callingExpression;
        
        IndexOfMethod(String s, BuiltInExpression callingExpression) {
            this.s = s;
            this.callingExpression = callingExpression;
        }
        
        public Object exec(List args) throws TemplateModelException {
            Object obj;
            String sub;
            int fidx;

            int ln  = args.size();
            if (ln == 0) {
                throw new TemplateModelException(
                        "?index_of(...) expects at least one argument.");
            }
            if (ln > 2) {
                throw new TemplateModelException(
                        "?index_of(...) expects at most two arguments.");
            }

            obj = args.get(0);       
            if (!(obj instanceof TemplateScalarModel)) {
                throw new TemplateModelException(
                        "?" + callingExpression.getName() + "(...) expects a string as "
                        + "its first argument.");
            }
            sub = ((TemplateScalarModel) obj).getAsString();
            
            if (ln > 1) {
                obj = args.get(1);
                if (!(obj instanceof TemplateNumberModel)) {
                    throw new TemplateModelException(
                            "?" + callingExpression.getName() + "(...) expects a number as "
                            + "its second argument.");
                }
                fidx = ((TemplateNumberModel) obj).getAsNumber().intValue();
            } else {
                fidx = 0;
            }
            int index;
            if (callingExpression.getName() == "last_index_of") {
            	if (ln >1)
            		index = s.lastIndexOf(sub, fidx); 
            	else 
            		index = s.lastIndexOf(sub);
            } else {
            	index = s.indexOf(sub, fidx);
            }
            return new SimpleNumber(index);
        }
    }
    
    static class ContainsMethod implements TemplateMethodModelEx {
        private String s;

        private ContainsMethod(String s) {
            this.s = s;
        }

        public Object exec(List args) throws TemplateModelException {
            Object obj;
            String sub;

            int ln  = args.size();
            if (ln != 1) {
                throw new TemplateModelException(
                        "?contains(...) expects one argument.");
            }

            obj = args.get(0);
            if (!(obj instanceof TemplateScalarModel)) {
                throw new TemplateModelException(
                        "?contains(...) expects a string as "
                        + "its first argument.");
            }
            sub = ((TemplateScalarModel) obj).getAsString();

            return
                (s.indexOf(sub) != -1) ?
                TemplateBooleanModel.TRUE :
                TemplateBooleanModel.FALSE;
        }
    }
}
