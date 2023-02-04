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
import freemarker.core.ast.TemplateNode;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;
import freemarker.template.utility.StringUtil;

/**
 * Implementations of ?substring and other 
 * standard functions that operate on strings
 */
public abstract class StringFunctions extends ExpressionEvaluatingBuiltIn {

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


    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) throws TemplateException
    {
        try {
            String string = ((TemplateScalarModel) model).getAsString();
            return apply(string, env, caller);
        } catch (ClassCastException cce) {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "string");
        } catch (NullPointerException npe) {
            throw new InvalidReferenceException("String is undefined", env);
        }
    }

    public abstract TemplateModel apply(final String string, final Environment env, final BuiltInExpression callingExpression) throws TemplateException;
    
    public static class Length extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new SimpleNumber(Integer.valueOf(string.length()));
        }
    }

    public static class StartsWith extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new StartsOrEndsWithMethod(string, false);
        }
    }

    public static class EndsWith extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new StartsOrEndsWithMethod(string, true);
        }
    }

    public static class Substring extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new SubstringMethod(string);
        }
    }

    public static class Replace extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new ReplaceMethod(string);
        }
    }

    public static class Split extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new SplitMethod(string);
        }
    }

    public static class Matches extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new MatcherBuilder(string);
        }
    }

    public static class IndexOf extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new IndexOfMethod(string, false);
        }
    }

    public static class LastIndexOf extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new IndexOfMethod(string, true);
        }
    }

    public static class Contains extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new ContainsMethod(string);
        }
    }

    public static class LeftPad extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new LeftPadMethod(string);
        }
    }

    public static class RightPad extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new RightPadMethod(string);
        }
    }

    public static class WordList extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            StringTokenizer st = new StringTokenizer(string);
            SimpleSequence result = new SimpleSequence();
            while (st.hasMoreTokens()) {
                result.add(st.nextToken());
            }
            return result;
        }
    }

    public static class Url extends StringFunctions {
        @Override
        public TemplateModel apply(String string, Environment env, BuiltInExpression caller) {
            return new urlBIResult(string, env);
        }
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

        private final String string;

        SubstringMethod(String string) {
            this.string = string;
        }

        public Object exec(java.util.List args) throws TemplateModelException {
            int argCount = args.size(), left=0, right=0;
            if (argCount != 1 && argCount != 2) {
                throw new TemplateModelException("Expecting 1 or 2 numerical arguments for ?substring(...)");
            }
            try {
                TemplateNumberModel tnm = (TemplateNumberModel) args.get(0);
                left = tnm.getAsNumber().intValue();
                if (argCount == 2) {
                    tnm = (TemplateNumberModel) args.get(1);
                    right = tnm.getAsNumber().intValue();
                }
            } catch (ClassCastException cce) {
                throw new TemplateModelException("Expecting numerical arguments for ?substring(...)");
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
        private final String string;
        private final boolean reverse;

        private StartsOrEndsWithMethod(String string, boolean reverse) {
            this.string = string;
            this.reverse = reverse;
        }

        private String getName() {
            return reverse ? "?ends_with" : "?starts_with";
        }
        
        public Object exec(List args) throws TemplateModelException {
            String sub;

            if (args.size() != 1) {
                throw new TemplateModelException(
                        getName()+ "(...) expects exactly 1 argument.");
            }

            Object obj = args.get(0);
            if (!(obj instanceof TemplateScalarModel)) {
                throw new TemplateModelException(
                        getName() + "(...) expects a string argument");
            }
            sub = ((TemplateScalarModel) obj).getAsString();

            boolean result = reverse ? string.endsWith(sub) : string.startsWith(sub);
            return result ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
        }
    }

    static class IndexOfMethod implements TemplateMethodModelEx {
        private final String s;
        private final boolean reverse;

        IndexOfMethod(String s, boolean reverse) {
            this.s = s;
            this.reverse = reverse;
        }

        private String getName() {
            return "?" + (reverse ? "last_" : "") + "index_of";
        }
        
        public Object exec(List args) throws TemplateModelException {
            Object obj;
            String sub;
            int fidx;

            int ln  = args.size();
            if (ln == 0) {
                throw new TemplateModelException(getName() + "(...) expects at least one argument.");
            }
            if (ln > 2) {
                throw new TemplateModelException(getName() + "(...) expects at most two arguments.");
            }

            obj = args.get(0);       
            if (!(obj instanceof TemplateScalarModel)) {
                throw new TemplateModelException(getName() + "(...) expects a string as "
                        + "its first argument.");
            }
            sub = ((TemplateScalarModel) obj).getAsString();

            if (ln > 1) {
                obj = args.get(1);
                if (!(obj instanceof TemplateNumberModel)) {
                    throw new TemplateModelException(getName() + "(...) expects a number as "
                            + "its second argument.");
                }
                fidx = ((TemplateNumberModel) obj).getAsNumber().intValue();
            } else {
                fidx = 0;
            }
            int index;
            if (reverse) {
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
