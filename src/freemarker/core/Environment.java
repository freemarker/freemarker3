package freemarker.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import freemarker.core.nodes.generated.ArgsList;
import freemarker.core.nodes.generated.Block;
import freemarker.core.nodes.generated.IncludeInstruction;
import freemarker.core.nodes.generated.Macro;
import freemarker.core.nodes.ParameterList;
import freemarker.core.nodes.generated.TemplateElement;
import freemarker.core.nodes.generated.UnifiedCall;
import freemarker.log.Logger;
import freemarker.core.variables.*;
import freemarker.core.variables.scope.*;
import freemarker.template.*;

import static freemarker.core.variables.Wrap.*;

/**
 * Object that represents the runtime environment during template processing.
 * For every invocation of a <tt>Template.process()</tt> method, a new
 * instance of this object is created, and then discarded when
 * <tt>process()</tt> returns. This object stores the set of temporary
 * variables created by the template, the value of settings set by the template,
 * the reference to the data model root, etc. Everything that is needed to
 * fulfill the template processing job.
 * 
 * <p>
 * Data models that need to access the <tt>Environment</tt> object that
 * represents the template processing on the current thread can use the
 * {@link #getCurrentEnvironment()} method.
 * 
 * <p>
 * If you need to modify or read this object before or after the
 * <tt>process</tt> call, use
 * 
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 * @author Attila Szegedi
 */
public final class Environment extends Configurable implements Scope {
    private static final ThreadLocal<Environment> threadEnv = new ThreadLocal<Environment>();

    static final Logger logger = Logger.getLogger("freemarker.runtime");
    private static final Logger attemptLogger = Logger.getLogger("freemarker.runtime.attempt");

    private static final Map<NumberFormatKey, NumberFormat> localizedNumberFormats = new HashMap<NumberFormatKey, NumberFormat>();

    private static final Map<DateFormatKey, DateFormat> localizedDateFormats = new HashMap<DateFormatKey, DateFormat>();

    // Do not use this object directly; clone it first! DecimalFormat isn't
    // thread-safe.
    private static final DecimalFormat C_NUMBER_FORMAT = new DecimalFormat(
            "0.################",
            new DecimalFormatSymbols(Locale.US));
    static {
        C_NUMBER_FORMAT.setGroupingUsed(false);
        C_NUMBER_FORMAT.setDecimalSeparatorAlwaysShown(false);
    }

    private final Map<String,Object> rootDataModel;

    private final List<TemplateElement> elementStack = new ArrayList<TemplateElement>();

    private final List<String> recoveredErrorStack = new ArrayList<String>();

    private NumberFormat numberFormat;

    private Map<String, NumberFormat> numberFormats;

    private DateFormat timeFormat, dateFormat, dateTimeFormat;

    private Map<String, DateFormat>[] dateFormats;

    private NumberFormat cNumberFormat;

    private Collator collator;

    private Writer out;

    private MacroContext currentMacroContext;

    private Scope mainNamespace;

    private Scope currentScope;

    private Map<Macro, MacroContext> macroContextLookup = new HashMap<>();

    private Map<Macro, Scope> macroToNamespaceLookup = new HashMap<>();

    private HashMap<String, Object> globalVariables = new HashMap<>();

    private HashMap<String, Scope> loadedLibs;

    private Throwable lastThrowable;

    private Object lastReturnValue;

    private WrappedNode currentVisitorNode;

    private List<Scope> nodeNamespaces;

    // Things we keep track of for the fallback mechanism.
    private int nodeNamespaceIndex;

    private String currentNodeName, currentNodeNS;

    private String cachedURLEscapingCharset;

    private boolean urlEscapingCharsetCached;

    /**
     * Retrieves the environment object associated with the current thread. Data
     * model implementations that need access to the environment can call this
     * method to obtain the environment object that represents the template
     * processing that is currently running on the current thread.
     */
    public static Environment getCurrentEnvironment() {
        return threadEnv.get();
    }

    public Environment(Template template, Map<String,Object> rootDataModel, Writer out) {
        super(template);
        //this.currentScope = mainNamespace = new TemplateNamespace(this, template);
        this.currentScope = mainNamespace = new BlockScope(template.getRootElement(), this);
        //System.out.println("Current scope is " + currentScope);
        this.out = out;
        this.rootDataModel = rootDataModel;
        importMacros(template);
    }

    /**
     * Retrieves the currently processed template.
     */
    public Template getTemplate() {
        return (Template) getFallback();
    }

    /**
     * Deletes cached values that are meant to be valid only during a single
     * template execution.
     */
    private void clearCachedValues() {
        numberFormats = null;
        numberFormat = null;
        dateFormats = null;
        collator = null;
        cachedURLEscapingCharset = null;
        urlEscapingCharsetCached = false;
    }

    /**
     * Processes the template to which this environment belongs.
     */
    public void process() throws TemplateException, IOException {
        Environment savedEnv = threadEnv.get();
        threadEnv.set(this);
        try {
            // Cached values from a previous execution are possibly outdated.
            clearCachedValues();
            try {
                doAutoImportsAndIncludes(this);
                Template template = getTemplate();
                render(template.getRootElement());
                // Do not flush if there was an exception.
                out.flush();
            } finally {
                // It's just to allow the GC to free memory...
                clearCachedValues();
            }
        } finally {
            threadEnv.set(savedEnv);
        }
    }

    /**
     * "Visit" the template element.
     */
    public void render(TemplateElement element) throws IOException {
        pushElement(element);
        Block nestedBlock = element.getNestedBlock();
        boolean createNewScope = nestedBlock != null 
                                 && !nestedBlock.isTemplateRoot()
                                 && !(nestedBlock.getParent() instanceof Macro)
                                 && nestedBlock.createsScope();
        Scope prevScope = currentScope;
        if (createNewScope) {
            currentScope = new BlockScope(nestedBlock, currentScope);
        }
        try {
            element.execute(this);
        } catch (TemplateException te) {
            handleTemplateException(te);
        } finally {
            popElement();
            currentScope = prevScope;
        }
    }

    private static final Object[] NO_OUT_ARGS = new Object[0];

    public void render(final Block block,
            UserDirective directiveModel, Map<String, Object> args,
            final List<String> bodyParameterNames)
            throws IOException {
        UserDirectiveBody nested = null;
        boolean createsNewScope = false;
        if (block != null) {
            createsNewScope = block.createsScope();
            nested = new UserDirectiveBody() {
                public void render(Writer newOut) throws TemplateException, IOException {
                    Writer prevOut = out;
                    out = newOut;
                    try {
                        Environment.this.render(block);
                    } finally {
                        out.flush();
                        out = prevOut;
                    }
                }
            };
        }
        final Object[] outArgs;
        if (bodyParameterNames == null || bodyParameterNames.isEmpty()) {
            outArgs = NO_OUT_ARGS;
        } else {
            outArgs = new Object[bodyParameterNames.size()];
        }
        final Scope scope = currentScope;
        if (createsNewScope) {
            currentScope = new NamedParameterListScope(scope,
                    bodyParameterNames, Arrays.asList(outArgs), true);
        }
        try {
            directiveModel.execute(this, args, outArgs, nested);
        } finally {
            currentScope = scope;
        }
    }


    /**
     * Visit a block using buffering/recovery
     */
    public void render(TemplateElement attemptBlock,
            TemplateElement recoveryBlock) throws IOException {
        Writer prevOut = this.out;
        StringWriter sw = new StringWriter();
        this.out = sw;
        TemplateException thrownException = null;
        try {
            render(attemptBlock);
        } catch (TemplateException te) {
            thrownException = te;
        } finally {
            this.out = prevOut;
        }
        if (thrownException != null) {
            if (attemptLogger.isDebugEnabled()) {
                logger.debug("Error in attempt block " +
                        attemptBlock.getLocation(), thrownException);
            }
            try {
                recoveredErrorStack.add(thrownException.getMessage());
                render(recoveryBlock);
            } finally {
                recoveredErrorStack.remove(recoveredErrorStack.size() - 1);
            }
        } else {
            out.write(sw.toString());
        }
    }

    public String getCurrentRecoveredErrorMessage() {
        if (recoveredErrorStack.isEmpty()) {
            throw new TemplateException(
                    ".error is not available outside of a <#recover> block",
                    this);
        }
        return recoveredErrorStack.get(recoveredErrorStack.size() - 1);
    }

    public void render(MacroInvocationBodyContext bctxt)
            throws TemplateException, IOException {
        MacroContext invokingMacroContext = getCurrentMacroContext();
        TemplateElement body = invokingMacroContext.getBody();
        if (body != null) {
            this.currentMacroContext = invokingMacroContext.getInvokingMacroContext();
            Configurable prevParent = getFallback();
            Scope prevScope = currentScope;
            setFallback(getCurrentNamespace().getTemplate());
            currentScope = bctxt;
            try {
                render(body);
            } finally {
                currentScope = prevScope;
                this.currentMacroContext = invokingMacroContext;
                setFallback(prevParent);
                this.currentScope = prevScope;
            }
        }
    }

    /**
     * Loop over a block, using the iterator passed in and
     * the given variable name for the loop variable.
     */
    public void process(Iterator<?> it, Block block, String loopVarName) throws IOException {
        Scope prevScope = currentScope;
        int index = 0;
        String hasNextName = loopVarName + "_has_next";
        String indexName = loopVarName + "_index";
        try {
            while (it.hasNext()) {
                currentScope = new BlockScope(block, prevScope);
                currentScope.put(loopVarName, wrap(it.next()));
                currentScope.put(hasNextName, it.hasNext());
                currentScope.put(indexName, index++);
                render(block);
            }
        } catch (BreakException br) {
        } catch (TemplateException te) {
            handleTemplateException(te);
        } finally {
            currentScope = prevScope;
        }
    }

    /**
     * "Visit" A WrappedNode
     */
    public void render(WrappedNode node, List<Scope> namespaces)
            throws TemplateException, IOException {
        if (nodeNamespaces == null) {
            List<Scope> ss = new ArrayList<>();
            ss.add(getCurrentNamespace());
            nodeNamespaces = ss;
        }
        int prevNodeNamespaceIndex = this.nodeNamespaceIndex;
        String prevNodeName = this.currentNodeName;
        String prevNodeNS = this.currentNodeNS;
        List<Scope> prevNodeNamespaces = nodeNamespaces;
        WrappedNode prevVisitorNode = currentVisitorNode;
        currentVisitorNode = node;
        if (namespaces != null) {
            this.nodeNamespaces = namespaces;
        }
        try {
            Object macroOrTransform = getNodeProcessor(node);
            if (macroOrTransform instanceof Macro) {
                render((Macro) macroOrTransform, (ArgsList) null, null, null);
            } else {
                String nodeType = node.getNodeType();
                if (nodeType != null) {
                    // If the node's type is 'text', we just output it.
                    if (nodeType.equals("text")) {
                        out.write(nodeType.toString());
                    } else if (nodeType.equals("document")) {
                        process(node, namespaces);
                    }
                    // We complain here, unless the node's type is 'pi', or
                    // "comment" or "document_type", in which case
                    // we just ignore it.
                    else if (!nodeType.equals("pi")
                            && !nodeType.equals("comment")
                            && !nodeType.equals("document_type")) {
                        String nsBit = "";
                        String ns = node.getNodeNamespace();
                        if (ns != null) {
                            if (ns.length() > 0) {
                                nsBit = " and namespace " + ns;
                            } else {
                                nsBit = " and no namespace";
                            }
                        }
                        throw new TemplateException(
                                "No macro or transform defined for node named "
                                        + node.getNodeName()
                                        + nsBit
                                        + ", and there is no fallback handler called @"
                                        + nodeType + " either.",
                                this);
                    }
                } else {
                    String nsBit = "";
                    String ns = node.getNodeNamespace();
                    if (ns != null) {
                        if (ns.length() > 0) {
                            nsBit = " and namespace " + ns;
                        } else {
                            nsBit = " and no namespace";
                        }
                    }
                    throw new TemplateException(
                            "No macro or transform defined for node with name "
                                    + node.getNodeName()
                                    + nsBit
                                    + ", and there is no macro or transform called @default either.",
                            this);
                }
            }
        } finally {
            this.currentVisitorNode = prevVisitorNode;
            this.nodeNamespaceIndex = prevNodeNamespaceIndex;
            this.currentNodeName = prevNodeName;
            this.currentNodeNS = prevNodeNS;
            this.nodeNamespaces = prevNodeNamespaces;
        }
    }

    public <T> T runInScope(Scope scope, TemplateRunnable<T> runnable)
            throws TemplateException, IOException {
        Scope currentScope = this.currentScope;
        this.currentScope = scope;
        try {
            return runnable.run();
        } finally {
            this.currentScope = currentScope;
        }
    }

    @SuppressWarnings("deprecation")
    public void fallback() throws IOException {
        Object macroOrTransform = getNodeProcessor(currentNodeName,
                currentNodeNS, nodeNamespaceIndex);
        if (macroOrTransform instanceof Macro) {
            render((Macro) macroOrTransform, (ArgsList) null, null, null);
        } 
    }

    /**
     * "visit" a macro.
     */
    public void render(Macro macro, ArgsList args,
            ParameterList bodyParameters,
            TemplateElement nestedBlock)
            throws TemplateException, IOException {
        if (macro == Macro.DO_NOTHING_MACRO) {
            return;
        }
        pushElement(macro);
        try {
            MacroContext mc = new MacroContext(macro, this, nestedBlock, bodyParameters);
            MacroContext prevMc = macroContextLookup.get(macro);
            macroContextLookup.put(macro, mc);
            if (args != null) {
                Map<String, Object> argsMap = macro.getParams().getParameterMap(args, this);
                for (Map.Entry<String, Object> entry : argsMap.entrySet()) {
                    mc.put(entry.getKey(), entry.getValue());
                }
            }
            Scope prevScope = currentScope;
            Configurable prevParent = getFallback();
            currentScope = currentMacroContext = mc;
            try {
                render(macro.getNestedBlock());                
            } catch (ReturnException re) {
            } catch (TemplateException te) {
                handleTemplateException(te);
            } finally {
                if (prevMc != null) {
                    macroContextLookup.put(macro, prevMc);
                } else {
                    macroContextLookup.remove(macro);
                }
                currentMacroContext = mc.getInvokingMacroContext();
                currentScope = prevScope;
                setFallback(prevParent);
            }
        } finally {
            popElement();
        }
    }

    public void visitMacroDef(Macro macro) {
        if (currentMacroContext == null) {
            macroToNamespaceLookup.put(macro, getCurrentNamespace());
            // getCurrentNamespace().put(macro.getName(), macro);
            this.unqualifiedSet(macro.getName(), macro);
        }
    }

    public Scope getMacroNamespace(Macro macro) {
        Scope result = macroToNamespaceLookup.get(macro);
        if (result == null) {
            result = mainNamespace; // REVISIT ??
        }
        return result;
    }

    public MacroContext getMacroContext(Macro macro) {
        return macroContextLookup.get(macro);
    }

    public void process(WrappedNode node, List<Scope> namespaces)
            throws TemplateException, IOException {
        if (node == null) {
            node = this.getCurrentVisitorNode();
            if (node == null) {
                throw new EvaluationException(
                        "The target node of recursion is missing or null.");
            }
        }
        List<WrappedNode> children = node.getChildNodes();
        if (children == null)
            return;
        for (int i = 0; i < children.size(); i++) {
            WrappedNode child = (WrappedNode) children.get(i);
            if (child != null) {
                render(child, namespaces);
            }
        }
    }

    public MacroContext getCurrentMacroContext() {
        return currentMacroContext;
    }

    private void handleTemplateException(TemplateException te) {
        // Logic to prevent double-handling of the exception in
        // nested visit() calls.
        if (lastThrowable == te) {
            throw te;
        }
        lastThrowable = te;

        // Log the exception
        if (logger.isErrorEnabled()) {
            logger.error(te.getMessage(), te);
        }

        // An assertion failing is not passed to the handler, but
        // explicitly rethrown.
        if (te instanceof AssertionFailedException) {
            throw te;
        }

        // Finally, pass the exception to the handler
        getTemplateExceptionHandler().handleTemplateException(te, this, out);
    }

    public void setTemplateExceptionHandler(
            TemplateExceptionHandler templateExceptionHandler) {
        super.setTemplateExceptionHandler(templateExceptionHandler);
        lastThrowable = null;
    }

    public void setLocale(Locale locale) {
        super.setLocale(locale);
        // Clear local format cache
        numberFormats = null;
        numberFormat = null;

        dateFormats = null;
        timeFormat = dateFormat = dateTimeFormat = null;

        collator = null;
    }

    public void setTimeZone(TimeZone timeZone) {
        super.setTimeZone(timeZone);
        // Clear local date format cache
        dateFormats = null;
        timeFormat = dateFormat = dateTimeFormat = null;
    }

    public void setURLEscapingCharset(String urlEscapingCharset) {
        urlEscapingCharsetCached = false;
        super.setURLEscapingCharset(urlEscapingCharset);
    }

    /*
     * Note that altough it is not allowed to set this setting with the
     * <tt>setting</tt>
     * directive, it still must be allowed to set it from Java code while the
     * template executes, since some frameworks allow templates to actually
     * change the output encoding on-the-fly.
     */
    public void setOutputEncoding(String outputEncoding) {
        urlEscapingCharsetCached = false;
        super.setOutputEncoding(outputEncoding);
    }

    /**
     * Returns the name of the charset that should be used for URL encoding.
     * This will be <code>null</code> if the information is not available. The
     * function caches the return value, so it is quick to call it repeately.
     */
    public String getEffectiveURLEscapingCharset() {
        if (!urlEscapingCharsetCached) {
            cachedURLEscapingCharset = getURLEscapingCharset();
            if (cachedURLEscapingCharset == null) {
                cachedURLEscapingCharset = getOutputEncoding();
            }
            urlEscapingCharsetCached = true;
        }
        return cachedURLEscapingCharset;
    }

    public Collator getCollator() {
        if (collator == null) {
            collator = Collator.getInstance(getLocale());
        }
        return collator;
    }

    public void setOut(Writer out) {
        this.out = out;
    }

    public Writer getOut() {
        return out;
    }

    public String formatNumber(Number number) {
        if (numberFormat == null) {
            numberFormat = getNumberFormatObject(getNumberFormat());
        }
        return numberFormat.format(number);
    }

    public void setNumberFormat(String formatName) {
        super.setNumberFormat(formatName);
        numberFormat = null;
    }

    public String formatDate(Date date, int type) {
        DateFormat df = getDateFormatObject(type);
        if (df == null) {
            throw new EvaluationException(
                    "Can't convert the date to string, because it is not known which parts of the date variable are in use. Use ?date, ?time or ?datetime built-in, or ?string.<format> or ?string(format) built-in with this date.");
        }
        return df.format(date);
    }

    public void setTimeFormat(String formatName) {
        super.setTimeFormat(formatName);
        timeFormat = null;
    }

    public void setDateFormat(String formatName) {
        super.setDateFormat(formatName);
        dateFormat = null;
    }

    public void setDateTimeFormat(String formatName) {
        super.setDateTimeFormat(formatName);
        dateTimeFormat = null;
    }

    public Configuration getConfiguration() {
        return getTemplate().getConfiguration();
    }

    public Object getLastReturnValue() {
        return lastReturnValue;
    }

    public void setLastReturnValue(Object lastReturnValue) {
        this.lastReturnValue = lastReturnValue;
    }

    void clearLastReturnValue() {
        this.lastReturnValue = null;
    }

    public NumberFormat getNumberFormatObject(String pattern) {
        if (numberFormats == null) {
            numberFormats = new HashMap<String, NumberFormat>();
        }

        NumberFormat format = numberFormats.get(pattern);
        if (format != null) {
            return format;
        }

        // Get format from global format cache
        synchronized (localizedNumberFormats) {
            Locale locale = getLocale();
            NumberFormatKey fk = new NumberFormatKey(pattern, locale);
            format = localizedNumberFormats.get(fk);
            if (format == null) {
                // Add format to global format cache. Note this is
                // globally done once per locale per pattern.
                if ("number".equals(pattern)) {
                    format = NumberFormat.getNumberInstance(locale);
                } else if ("currency".equals(pattern)) {
                    format = NumberFormat.getCurrencyInstance(locale);
                } else if ("percent".equals(pattern)) {
                    format = NumberFormat.getPercentInstance(locale);
                } else if ("computer".equals(pattern)) {
                    format = getCNumberFormat();
                } else {
                    format = new DecimalFormat(pattern,
                            new DecimalFormatSymbols(getLocale()));
                }
                localizedNumberFormats.put(fk, format);
            }
        }

        // Clone it and store the clone in the local cache
        format = (NumberFormat) format.clone();
        numberFormats.put(pattern, format);
        return format;
    }

    public DateFormat getDateFormatObject(int dateType) {
        switch (dateType) {
            case WrappedDate.UNKNOWN: {
                return null;
            }
            case WrappedDate.TIME: {
                if (timeFormat == null) {
                    timeFormat = getDateFormatObject(dateType, getTimeFormat());
                }
                return timeFormat;
            }
            case WrappedDate.DATE: {
                if (dateFormat == null) {
                    dateFormat = getDateFormatObject(dateType, getDateFormat());
                }
                return dateFormat;
            }
            case WrappedDate.DATETIME: {
                if (dateTimeFormat == null) {
                    dateTimeFormat = getDateFormatObject(dateType,
                            getDateTimeFormat());
                }
                return dateTimeFormat;
            }
            default: {
                throw new EvaluationException("Unrecognized date type "
                        + dateType);
            }
        }
    }

    public DateFormat getDateFormatObject(int dateType, String pattern) {
        if (dateFormats == null) {
            dateFormats = new Map[4];
            dateFormats[WrappedDate.UNKNOWN] = new HashMap<String, DateFormat>();
            dateFormats[WrappedDate.TIME] = new HashMap<String, DateFormat>();
            dateFormats[WrappedDate.DATE] = new HashMap<String, DateFormat>();
            dateFormats[WrappedDate.DATETIME] = new HashMap<String, DateFormat>();
        }
        Map<String, DateFormat> typedDateFormat = dateFormats[dateType];

        DateFormat format = typedDateFormat.get(pattern);
        if (format != null) {
            return format;
        }

        // Get format from global format cache
        synchronized (localizedDateFormats) {
            Locale locale = getLocale();
            TimeZone timeZone = getTimeZone();
            DateFormatKey fk = new DateFormatKey(dateType, pattern, locale,
                    timeZone);
            format = localizedDateFormats.get(fk);
            if (format == null) {
                // Add format to global format cache. Note this is
                // globally done once per locale per pattern.
                StringTokenizer tok = new StringTokenizer(pattern, "_");
                int style = tok.hasMoreTokens() ? parseDateStyleToken(tok
                        .nextToken()) : DateFormat.DEFAULT;
                if (style != -1) {
                    switch (dateType) {
                        case WrappedDate.UNKNOWN: {
                            throw new EvaluationException(
                                    "Can't convert the date to string using a "
                                            + "built-in format, because it is not known which "
                                            + "parts of the date variable are in use. Use "
                                            + "?date, ?time or ?datetime built-in, or "
                                            + "?string.<format> or ?string(<format>) built-in "
                                            + "with explicit formatting pattern with this date.");
                        }
                        case WrappedDate.TIME: {
                            format = DateFormat.getTimeInstance(style, locale);
                            break;
                        }
                        case WrappedDate.DATE: {
                            format = DateFormat.getDateInstance(style, locale);
                            break;
                        }
                        case WrappedDate.DATETIME: {
                            int timestyle = tok.hasMoreTokens() ? parseDateStyleToken(tok
                                    .nextToken())
                                    : style;
                            if (timestyle != -1) {
                                format = DateFormat.getDateTimeInstance(style,
                                        timestyle, locale);
                            }
                            break;
                        }
                    }
                }
                if (format == null) {
                    try {
                        format = new SimpleDateFormat(pattern, locale);
                    } catch (IllegalArgumentException e) {
                        throw new EvaluationException("Can't parse "
                                + pattern + " to a date format.", e);
                    }
                }
                format.setTimeZone(timeZone);
                localizedDateFormats.put(fk, format);
            }
        }

        // Clone it and store the clone in the local cache
        format = (DateFormat) format.clone();
        typedDateFormat.put(pattern, format);
        return format;
    }

    int parseDateStyleToken(String token) {
        if ("short".equals(token)) {
            return DateFormat.SHORT;
        }
        if ("medium".equals(token)) {
            return DateFormat.MEDIUM;
        }
        if ("long".equals(token)) {
            return DateFormat.LONG;
        }
        if ("full".equals(token)) {
            return DateFormat.FULL;
        }
        return -1;
    }

    /**
     * Returns the {@link NumberFormat} used for the <tt>c</tt> built-in.
     * This is always US English <code>"0.################"</code>, without
     * grouping and without superfluous decimal separator.
     */
    public NumberFormat getCNumberFormat() {
        // It can't be cached in a static field, because DecimalFormat-s aren't
        // thread-safe.
        if (cNumberFormat == null) {
            cNumberFormat = getNewCNumberFormat();
        }
        return cNumberFormat;
    }

    public static NumberFormat getNewCNumberFormat() {
        return (NumberFormat) C_NUMBER_FORMAT.clone();
    }

    public Object resolveVariable(String name) {
        return get(name);
    }

    /**
     * Returns the variable that is visible in this context. This is the
     * correspondent to an FTL top-level variable reading expression. That is,
     * it tries to find the the variable in this order:
     * <ol>
     * <li>An loop variable (if we're in a loop or user defined directive body)
     * such as foo_has_next
     * <li>A local variable (if we're in a macro)
     * <li>A variable defined in the current namespace (say, via &lt;#assign
     * ...&gt;)
     * <li>A variable defined globally (say, via &lt;#global ....&gt;)
     * <li>Variable in the data model:
     * <ol>
     * <li>A variable in the root hash that was exposed to this rendering
     * environment in the Template.process(...) call
     * <li>A shared variable set in the configuration via a call to
     * Configuration.setSharedVariable(...)
     * </ol>
     * </li>
     * </ol>
     */
    public Object getVariable(String name) {
        return currentScope.resolveVariable(name);
    }

    /**
     * This method returns a variable from the "global" namespace and falls back
     * to the data model.
     */
    public Object get(String name) {
        Object result = globalVariables.get(name);
        if (result == null) {
            result = rootDataModel.get(name);
        }
        if (result == null) {
            result = getConfiguration().getSharedVariable(name);
        }
        return result;
    }

    /**
     * Sets a variable that is visible globally. This is correspondent to FTL
     * <code><#global <i>name</i>=<i>model</i>></code>.
     */
    public void setGlobalVariable(String name, Object model) {
        globalVariables.put(name, model);
    }

    /**
     * Sets a variable in the current namespace. This is correspondent to FTL
     * <code><#assign <i>name</i>=<i>model</i>></code>. This can be
     * considered a convenient shorthand for: getCurrentNamespace().put(name,
     * model)
     */
    public void setVariable(String name, Object model) {
        getCurrentNamespace().put(name, model);
    }

    /**
     * Sets a local variable (one effective only during a macro invocation).
     * This is correspondent to FTL
     * <code><#local <i>name</i>=<i>model</i>></code>.
     * 
     * @param name
     *              the identifier of the variable
     * @param model
     *              the value of the variable.
     * @throws IllegalStateException
     *                               if the environment is not executing a macro
     *                               body.
     */
    public void setLocalVariable(String name, Object model) {
        if (currentMacroContext == null) {
            throw new IllegalStateException("Not executing macro body");
        }
        currentMacroContext.put(name, model);
    }

    /**
     * Sets a variable in the most local scope available (corresponds to an
     * unqualified #set instruction)
     * 
     * @param name
     *              the identifier of the variable
     * @param model
     *              the value of the variable
     */
    public void unqualifiedSet(String name, Object model) {
        Scope scope = this.currentScope;
        while (!scope.isTemplateNamespace()) {
            if (scope.get(name) != null) {
                scope.put(name, model);
                return;
            }
            scope = scope.getEnclosingScope();
        }
        try {
            scope.put(name, model);
        } catch (UndeclaredVariableException uve) {
            if (globalVariables.containsKey(name)) {
                globalVariables.put(name, model);
            } else {
                throw new TemplateException(uve, this);
            }
        }
    }

    public Scope getCurrentScope() {
        return currentScope;
    }

    /**
     * Outputs the instruction stack. Useful for debugging.
     * {@link TemplateException}s incorporate this information in their stack
     * traces.
     * 
     * @see #getElementStack() which exposes the actual element stack
     *      so that you can write your own custom stack trace or error message
     */
    public void outputInstructionStack(PrintWriter pw) {
        pw.println("----------");
        ListIterator<TemplateElement> iter = elementStack
                .listIterator(elementStack.size());
        if (iter.hasPrevious()) {
            pw.print("==> ");
            TemplateElement prev = iter.previous();
            pw.print(prev.getDescription());
            pw.print(" [");
            pw.print(prev.getLocation());
            pw.println("]");
        }
        while (iter.hasPrevious()) {
            TemplateElement prev = iter.previous();
            if (prev instanceof UnifiedCall || prev instanceof IncludeInstruction) {
                String location = prev.getDescription() + " ["
                        + prev.getLocation() + "]";
                if (location != null && location.length() > 0) {
                    pw.print(" in ");
                    pw.println(location);
                }
            }
        }
        pw.println("----------");
        pw.flush();
    }

    public Environment getEnvironment() {
        return this;
    };

    /**
     * @return null This is the final fallback scope. It has no
     *         enclosing scope.
     */
    public Scope getEnclosingScope() {
        return null;
    }

    public boolean definesVariable(String name) {
        return globalVariables.containsKey(name) || rootDataModel.get(name) != null;
    }

    public Object put(String varname, Object value) {
        return globalVariables.put(varname, value);
    }

    public Object remove(String varname) {
        return globalVariables.remove(varname);
    }

    public boolean isEmpty() {
        return false; // REVISIT, is this right?
    }

    /**
     * Returns the name-space for the name if exists, or null.
     * 
     * @param name
     *             the template path that you have used with the
     *             <code>import</code> directive or
     *             {@link #importLib(String, String)} call, in normalized form.
     *             That is, the path must be an absolute path, and it must not
     *             contain "/../" or "/./". The leading "/" is optional.
     */
    public Scope getNamespace(String name) {
        if (name.startsWith("/"))
            name = name.substring(1);
        if (loadedLibs != null) {
            return loadedLibs.get(name);
        } else {
            return null;
        }
    }

    /**
     * Returns the main name-space. This is correspondent of FTL
     * <code>.main</code> hash.
     */
    public Scope getMainNamespace() {
        return mainNamespace;
    }

    /**
     * Returns the current name-space. This is correspondent of FTL
     * <code>.namespace</code> hash.
     */
    public Scope getCurrentNamespace() {
        Scope scope = currentScope;
        while (scope.getEnclosingScope() != this) {
            scope = scope.getEnclosingScope();
        }
        return scope;
    }

    /**
     * Returns a fictitious name-space that contains the globally visible
     * variables that were created in the template, but not the variables of the
     * data-model. There is no such thing in FTL; this strange method was added
     * because of the JSP taglib support, since this imaginary name-space
     * contains the page-scope attributes.
     */
    public Scope getGlobalNamespace() {
        return this; // REVISIT
    }

    /**
     * Returns the data model hash. This is correspondent of FTL
     * <code>.datamodel</code> hash. That is, it contains both the variables
     * of the root hash passed to the <code>Template.process(...)</code>, and
     * the shared variables in the <code>Configuration</code>.
     */
    public WrappedHash getDataModel() {
        final WrappedHash result = new WrappedHash() {
            public boolean isEmpty() {
                return false;
            }

            public Object get(String key) {
                Object value = rootDataModel.get(key);
                if (value == null) {
                    value = getConfiguration().getSharedVariable(key);
                }
                return value;
            }
        };

        if (rootDataModel instanceof WrappedHash) {
            return new WrappedHash() {
                public boolean isEmpty() {
                    return result.isEmpty();
                }

                public Object get(String key) {
                    return result.get(key);
                }

                // NB: The methods below do not take into account
                // configuration shared variables even though
                // the hash will return them, if only for BWC reasons
                public Iterable values() {
                    return ((WrappedHash) rootDataModel).values();
                }

                public Iterable keys() {
                    return ((WrappedHash) rootDataModel).keys();
                }

                public int size() {
                    return ((WrappedHash) rootDataModel).size();
                }
            };
        }
        return result;
    }

    public List<TemplateElement> getElementStack() {
        return Collections.unmodifiableList(elementStack);
    }

    private void pushElement(TemplateElement element) {
        elementStack.add(element);
    }

    private void popElement() {
        elementStack.remove(elementStack.size() - 1);
    }

    public WrappedNode getCurrentVisitorNode() {
        return currentVisitorNode;
    }

    /**
     * sets WrappedNode as the current visitor node.
     * <tt>.current_node</tt>
     */
    public void setCurrentVisitorNode(WrappedNode node) {
        currentVisitorNode = node;
    }

    Object getNodeProcessor(WrappedNode node) {
        String nodeName = node.getNodeName();
        if (nodeName == null) {
            throw new TemplateException("Node name is null.", this);
        }
        Object result = getNodeProcessor(nodeName, node
                .getNodeNamespace(), 0);
        if (result == null) {
            String type = node.getNodeType();
            if (type != null) {
                result = getNodeProcessor("@" + type, null, 0);
            }
            if (result == null) {
                result = getNodeProcessor("@default", null, 0);
            }
        }
        return result;
    }

    private Object getNodeProcessor(final String nodeName,
            final String nsURI, int startIndex) {
        Object result = null;
        int i;
        for (i = startIndex; i < nodeNamespaces.size(); i++) {
            Scope ns = null;
            try {
                ns = nodeNamespaces.get(i);
            } catch (ClassCastException cce) {
                throw new InvalidReferenceException(
                        "A using clause should contain a sequence of namespaces or strings that indicate the location of importable macro libraries.",
                        this);
            }
            result = getNodeProcessor(ns, nodeName, nsURI);
            if (result != null)
                break;
        }
        if (result != null) {
            this.nodeNamespaceIndex = i + 1;
            this.currentNodeName = nodeName;
            this.currentNodeNS = nsURI;
        }
        return result;
    }

    private Object getNodeProcessor(Scope ns, String localName, String nsURI) {
        Object result = null;
        if (nsURI == null) {
            result = ns.get(localName);
            if (!(result instanceof Macro)) {
                result = null;
            }
        } else {
            Template template = ns.getTemplate();
            String prefix = template.getPrefixForNamespace(nsURI);
            if (prefix == null) {
                // The other template cannot handle this node
                // since it has no prefix registered for the namespace
                return null;
            }
            if (prefix.length() > 0) {
                result = ns.get(prefix + ":" + localName);
                if (!(result instanceof Macro)) {
                    result = null;
                }
            } else {
                if (nsURI.length() == 0) {
                    result = ns.get(Template.NO_NS_PREFIX + ":" + localName);
                    if (!(result instanceof Macro)) {
                        result = null;
                    }
                }
                if (nsURI.equals(template.getDefaultNS())) {
                    result = ns.get(Template.DEFAULT_NAMESPACE_PREFIX + ":"
                            + localName);
                    if (!(result instanceof Macro)) {
                        result = null;
                    }
                }
                if (result == null) {
                    result = ns.get(localName);
                    if (!(result instanceof Macro)) {
                        result = null;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Emulates <code>include</code> directive, except that <code>name</code>
     * must be tempate root relative.
     * 
     * <p>
     * It's the same as
     * <code>include(getTemplateForInclusion(name, encoding, parse))</code>.
     * But, you may want to separately call these two methods, so you can
     * determine the source of exceptions more precisely, and thus achieve more
     * intelligent error handling.
     * 
     * @see #getTemplateForInclusion(String name, String encoding, boolean
     *      parse)
     * @see #include(Template includedTemplate, boolean freshNamespace)
     */
    public void include(String name, String encoding, boolean parse)
            throws IOException, TemplateException {
        include(getTemplateForInclusion(name, encoding, parse), false);
    }

    /**
     * Gets a template for inclusion; used with
     * {@link #include(Template includedTemplate, boolean freshNamespace)}. The
     * advantage over simply
     * using <code>config.getTemplate(...)</code> is that it chooses the
     * default encoding as the <code>include</code> directive does.
     * 
     * @param name
     *                 the name of the template, relatively to the template root
     *                 directory (not the to the directory of the currently
     *                 executing
     *                 template file!). (Note that you can use
     *                 {@link freemarker.cache.TemplateCache#getFullTemplatePath} to
     *                 convert paths to template root relative paths.)
     * @param encoding
     *                 the encoding of the obtained template. If null, the encoding
     *                 of the Template that is currently being processed in this
     *                 Environment is used.
     * @param parse
     *                 whether to process a parsed template or just include the
     *                 unparsed template source.
     */
    public Template getTemplateForInclusion(String name, String encoding,
            boolean parse) throws IOException {
        if (encoding == null) {
            encoding = getTemplate().getEncoding();
        }
        if (encoding == null) {
            encoding = getConfiguration().getEncoding(this.getLocale());
        }
        return getConfiguration().getTemplate(name, getLocale(), encoding,
                parse);
    }

    /**
     * Processes a Template in the context of this <code>Environment</code>,
     * including its output in the <code>Environment</code>'s Writer.
     * 
     * @param includedTemplate
     *                         the template to process. Note that it does
     *                         <em>not</em> need
     *                         to be a template returned by
     *                         {@link #getTemplateForInclusion(String name, String encoding, boolean parse)}.
     */
    public void include(Template includedTemplate, boolean freshNamespace) throws TemplateException,
            IOException {
        Template prevTemplate = getTemplate();
        setFallback(includedTemplate);
        Scope prevScope = this.currentScope;
        if (freshNamespace) {
            this.currentScope = new BlockScope(includedTemplate.getRootElement(), this);
            importMacros(includedTemplate);
        } else {
            //this.currentScope = new BlockScope(includedTemplate.getRootElement(), prevScope);
            this.currentScope = getCurrentNamespace();
            importMacros(includedTemplate);
        }
        try {
            render(includedTemplate.getRootElement());
        } finally {
            this.currentScope = prevScope;
            setFallback(prevTemplate);
        }
    }

    /**
     * Emulates <code>import</code> directive, except that <code>name</code>
     * must be tempate root relative.
     * 
     * <p>
     * It's the same as
     * <code>importLib(getTemplateForImporting(name), namespace)</code>. But,
     * you may want to separately call these two methods, so you can determine
     * the source of exceptions more precisely, and thus achieve more
     * intelligent error handling.
     * 
     * @see #getTemplateForImporting(String name)
     * @see #importLib(Template includedTemplate, String namespace, boolean global)
     */
    public Scope importLib(String name, String namespace)
            throws IOException, TemplateException {
        return importLib(getTemplateForImporting(name), namespace, true);
    }

    /**
     * Gets a template for importing; used with
     * {@link #importLib(Template importedTemplate, String namespace, boolean global)}.
     * The
     * advantage over simply using <code>config.getTemplate(...)</code> is
     * that it chooses the encoding as the <code>import</code> directive does.
     * 
     * @param name
     *             the name of the template, relatively to the template root
     *             directory (not the to the directory of the currently executing
     *             template file!). (Note that you can use
     *             {@link freemarker.cache.TemplateCache#getFullTemplatePath} to
     *             convert paths to template root relative paths.)
     */
    public Template getTemplateForImporting(String name) throws IOException {
        return getTemplateForInclusion(name, null, true);
    }

    /**
     * Emulates <code>import</code> directive.
     * 
     * @param loadedTemplate
     *                       the template to import. Note that it does <em>not</em>
     *                       need
     *                       to be a template returned by
     *                       {@link #getTemplateForImporting(String name)}.
     */
    public Scope importLib(Template loadedTemplate, String namespace, boolean global)
            throws IOException, TemplateException {
        if (loadedLibs == null) {
            loadedLibs = new HashMap<>();
        }
        String templateName = loadedTemplate.getName();
        Scope existingNamespace = loadedLibs.get(templateName);
        if (existingNamespace != null) {
            if (namespace != null) {
                setVariable(namespace, existingNamespace);
            }
        } else {
            Scope newNamespace = new BlockScope(loadedTemplate.getRootElement(), this);
            if (namespace != null) {
                if (global) {
                    setGlobalVariable(namespace, newNamespace);
                } else {
                    setVariable(namespace, newNamespace);
                }
                if (getCurrentNamespace() == mainNamespace) {
                    // We make libs imported into the main namespace globally visible
                    // for least surprise reasons. (Is this right???)
                    this.put(namespace, newNamespace);
                }
            }
            loadedLibs.put(templateName, newNamespace);
            Scope prevScope = currentScope;
            currentScope = newNamespace;
            Writer prevOut = out;
            Configurable prevParent = getFallback();
            this.out = NULL_WRITER;
            setFallback(loadedTemplate);
            try {
                render(loadedTemplate.getRootElement());
            } finally {
                this.out = prevOut;
                currentScope = prevScope;
                setFallback(prevParent);
            }
        }
        return loadedLibs.get(templateName);
    }

    public String renderElementToString(TemplateElement te) throws IOException,
            TemplateException {
        Writer prevOut = out;
        try {
            StringWriter sw = new StringWriter();
            this.out = sw;
            render(te);
            return sw.toString();
        } finally {
            this.out = prevOut;
        }
    }

    void importMacros(Template template) {
        for (Macro macro : template.getMacros().values()) {
            visitMacroDef(macro);
        }
    }

    /**
     * @return the namespace URI registered for this prefix, or null. This is
     *         based on the mappings registered in the current namespace.
     */
    public String getNamespaceForPrefix(String prefix) {
        return getCurrentNamespace().getTemplate()
                .getNamespaceForPrefix(prefix);
    }

    public String getPrefixForNamespace(String nsURI) {
        return getCurrentNamespace().getTemplate().getPrefixForNamespace(nsURI);
    }

    /**
     * @return the default node namespace for the current FTL namespace
     */
    public String getDefaultNS() {
        return getCurrentNamespace().getTemplate().getDefaultNS();
    }

    private static final class NumberFormatKey {
        private final String pattern;

        private final Locale locale;

        NumberFormatKey(String pattern, Locale locale) {
            this.pattern = pattern;
            this.locale = locale;
        }

        public boolean equals(Object o) {
            if (o instanceof NumberFormatKey) {
                NumberFormatKey fk = (NumberFormatKey) o;
                return fk.pattern.equals(pattern) && fk.locale.equals(locale);
            }
            return false;
        }

        public int hashCode() {
            return pattern.hashCode() ^ locale.hashCode();
        }
    }

    private static final class DateFormatKey {
        private final int dateType;

        private final String pattern;

        private final Locale locale;

        private final TimeZone timeZone;

        DateFormatKey(int dateType, String pattern, Locale locale,
                TimeZone timeZone) {
            this.dateType = dateType;
            this.pattern = pattern;
            this.locale = locale;
            this.timeZone = timeZone;
        }

        public boolean equals(Object o) {
            if (o instanceof DateFormatKey) {
                DateFormatKey fk = (DateFormatKey) o;
                return dateType == fk.dateType && fk.pattern.equals(pattern)
                        && fk.locale.equals(locale)
                        && fk.timeZone.equals(timeZone);
            }
            return false;
        }

        public int hashCode() {
            return dateType ^ pattern.hashCode() ^ locale.hashCode()
                    ^ timeZone.hashCode();
        }
    }

    static public final Writer NULL_WRITER = new Writer() {
        public void write(char cbuf[], int off, int len) {
        }

        public void flush() {
        }

        public void close() {
        }
    };

    private static final Writer EMPTY_BODY_WRITER = new Writer() {

        public void write(char[] cbuf, int off, int len) throws IOException {
            if (len > 0) {
                throw new IOException(
                        "This transform does not allow nested content.");
            }
        }

        public void flush() {
        }

        public void close() {
        }
    };
}
