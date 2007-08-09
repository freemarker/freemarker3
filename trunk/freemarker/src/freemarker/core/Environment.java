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

package freemarker.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.CodeSource;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import freemarker.core.ast.*;
import freemarker.core.parser.*;

import freemarker.ext.beans.BeansWrapper;
import freemarker.log.Logger;
import freemarker.template.*;
import freemarker.template.utility.UndeclaredThrowableException;

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
 * {@link Template#createProcessingEnvironment(Object rootMap, Writer out, ObjectWrapper wrapper)}
 * 
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 * @author Attila Szegedi
 */
public final class Environment extends Configurable implements Scope {
    private static final ThreadLocal<Environment> threadEnv = new ThreadLocal<Environment>();

    private static final Logger logger = Logger.getLogger("freemarker.runtime");

    private static final Map<NumberFormatKey, NumberFormat> localizedNumberFormats = new HashMap<NumberFormatKey, NumberFormat>();

    private static final Map<DateFormatKey, DateFormat> localizedDateFormats = new HashMap<DateFormatKey, DateFormat>();

    private final TemplateHashModel rootDataModel;

    private final List<TemplateElement> elementStack = new ArrayList<TemplateElement>();
    
    private final List<Expression> expressionStack = new ArrayList<Expression>();

    private final List<String> recoveredErrorStack = new ArrayList<String>();

    private CodeSource currentCodeSource;

    private NumberFormat numberFormat;

    private Map<String, NumberFormat> numberFormats;

    private DateFormat timeFormat, dateFormat, dateTimeFormat;

    private Map<String, DateFormat>[] dateFormats;

    private Collator collator;

    private Writer out;

    private MacroContext currentMacroContext;
    
    private TemplateNamespace mainNamespace;

    private Scope currentScope;

    private Map<Macro, MacroContext> macroContextLookup = new HashMap<Macro, MacroContext>();

    private Map<Macro, TemplateNamespace> macroToNamespaceLookup = new HashMap<Macro, TemplateNamespace>();

    private HashMap<String, TemplateModel> globalVariables = new HashMap<String, TemplateModel>();

    private HashMap<String, TemplateNamespace> loadedLibs;

    private Throwable lastThrowable;

    private TemplateModel lastReturnValue;

    private TemplateNodeModel currentVisitorNode;

    private TemplateSequenceModel nodeNamespaces;

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

    public Environment(Template template,
            final TemplateHashModel rootDataModel, Writer out) {
        super(template);
        this.currentScope = mainNamespace = new TemplateNamespace(
                this, template);
        this.out = out;
        this.rootDataModel = rootDataModel;
        resetCodeSource();
        importMacros(template);
    }

    /**
     * Retrieves the currently processed template.
     */
    public Template getTemplate() {
        return (Template) getParent();
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
        resetCodeSource();
    }

    private void resetCodeSource() {
        currentCodeSource = 
            getConfiguration().isSecure() 
                ? DefaultCodeSourceProvider.DEFAULT_CODE_SOURCE 
                : Template.NULL_CODE_SOURCE;
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
                Template template = getTemplate();
                renderSecurely(template.getRootElement(), template.getCodeSource());
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

    public void renderSecurely(TemplateElement element, CodeSource newCodeSource)
    throws TemplateException, IOException
    {
        // currentCodeSource can be null if we don't have enough privilege to
        // obtain DEFAULT_CODE_SOURCE. In that case, assume newCodeSource must
        // differ from it. Also, don't do any security related operations if
        // there's no security manager in the system.
        if(currentCodeSource == null || !currentCodeSource.equals(
                newCodeSource))
        {
            System.err.println("currentCodeSource=" + currentCodeSource + " newCodeSource=" + newCodeSource);
            // Code sources differ - incorporate the new code source's
            // protection domain onto the Java call stack
            CodeSource oldCodeSource = currentCodeSource;
            currentCodeSource = newCodeSource;
            try {
                SecureRenderer.renderSecurely(newCodeSource, this, element);
            }
            finally {
                currentCodeSource = oldCodeSource;
            }
        } else {
            // Equal code sources - just render in the current protection domain
            render(element);
        }
    }

    /**
     * "Visit" the template element.
     */
    public void render(TemplateElement element) throws TemplateException,
            IOException {
        pushElement(element);
        boolean createNewScope = element.createsScope();
        Scope prevScope = currentScope;
        if (createNewScope) {
            currentScope = element.createLocalScope(currentScope);
        }
        try {
            element.execute(this);
        } catch (TemplateException te) {
            handleTemplateException(te);
        } finally {
            popElement();
        }
    }

    public void render(final TemplateElement element,
            TemplateRunnableModel runnableModel, Map<String, TemplateModel> args)
            throws TemplateException, IOException {
        TemplateRunnableBody nested;
        if(element == null) {
            nested = null;
        }
        else {
            nested = new TemplateRunnableBody() {
                public void render(Writer newOut) throws TemplateException, IOException {
                    if(newOut == null) {
                        Environment.this.render(element);
                    }
                    else {
                        Writer prevOut = out;
                        out = newOut;
                        try {
                            Environment.this.render(element);
                        }
                        finally {
                            out = prevOut;
                        }
                    }
                }
            };
        }
        runnableModel.run(out, args, nested);
    }
    
    /**
     * "Visit" the template element, passing the output through a
     * TemplateTransformModel
     * 
     * @param element
     *            the element to visit through a transform
     * @param transform
     *            the transform to pass the element output through
     * @param args
     *            optional arguments fed to the transform
     */
    public void render(TemplateElement element,
            TemplateTransformModel transform, Map<String, TemplateModel> args)
            throws TemplateException, IOException {
        try {
            Writer tw = transform.getWriter(out, args);
            if (tw == null)
                tw = EMPTY_BODY_WRITER;
            TransformControl tc = tw instanceof TransformControl ? (TransformControl) tw
                    : null;

            Writer prevOut = out;
            out = tw;
            try {
                if (tc == null || tc.onStart() != TransformControl.SKIP_BODY) {
                    do {
                        if (element != null) {
                            render(element);
                        }
                    } while (tc != null
                            && tc.afterBody() == TransformControl.REPEAT_EVALUATION);
                }
            } catch (Throwable t) {
                try {
                    if (tc != null) {
                        tc.onError(t);
                    } else {
                        throw t;
                    }
                } catch (TemplateException e) {
                    throw e;
                } catch (IOException e) {
                    throw e;
                } catch (RuntimeException e) {
                    throw e;
                } catch (Error e) {
                    throw e;
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            } finally {
                out = prevOut;
                tw.close();
            }
        } catch (TemplateException te) {
            handleTemplateException(te);
        }
    }

    /**
     * Visit a block using buffering/recovery
     */

    public void render(TemplateElement attemptBlock,
            TemplateElement recoveryBlock, List<ParsingProblem> parsingProblems) throws TemplateException,
            IOException {
        Writer prevOut = this.out;
        StringWriter sw = new StringWriter();
        this.out = sw;
        TemplateException thrownException = null;
        try {
        	if (!parsingProblems.isEmpty()) {
        		throw new TemplateException(new MultiParseException(parsingProblems), this);
        	}
            render(attemptBlock);
        } catch (TemplateException te) {
            thrownException = te;
        } finally {
            this.out = prevOut;
        }
        if (thrownException != null) {
            if (logger.isErrorEnabled()) {
                String msg = "Error in attempt block "
                        + attemptBlock.getStartLocation();
                logger.error(msg, thrownException);
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

    public String getCurrentRecoveredErrorMessage() throws TemplateException {
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
        TemplateElement body = invokingMacroContext.body;
        if (body != null) {
            this.currentMacroContext = invokingMacroContext.invokingMacroContext;
            Configurable prevParent = getParent();
            Scope prevScope = currentScope;
            setParent(getCurrentNamespace().getTemplate());
             currentScope = bctxt;
            try {
                render(body);
            } finally {
                currentScope = prevScope;
                this.currentMacroContext = invokingMacroContext;
                setParent(prevParent);
                this.currentScope = prevScope;
            }
        }
    }

    /**
     * "visit" an IteratorBlock
     */
    public void process(LoopContext ictxt) throws TemplateException,
            IOException {
        Scope prevScope = currentScope;
        currentScope = ictxt;
        try {
            ictxt.runLoop();
        } catch (BreakException br) {
        } catch (TemplateException te) {
            handleTemplateException(te);
        } finally {
            currentScope = prevScope;
        }
    }

    /**
     * "Visit" A TemplateNodeModel
     */

    public void render(TemplateNodeModel node, TemplateSequenceModel namespaces)
            throws TemplateException, IOException {
        if (nodeNamespaces == null) {
            SimpleSequence ss = new SimpleSequence(1);
            ss.add(getCurrentNamespace());
            nodeNamespaces = ss;
        }
        int prevNodeNamespaceIndex = this.nodeNamespaceIndex;
        String prevNodeName = this.currentNodeName;
        String prevNodeNS = this.currentNodeNS;
        TemplateSequenceModel prevNodeNamespaces = nodeNamespaces;
        TemplateNodeModel prevVisitorNode = currentVisitorNode;
        currentVisitorNode = node;
        if (namespaces != null) {
            this.nodeNamespaces = namespaces;
        }
        try {
            TemplateModel macroOrTransform = getNodeProcessor(node);
            if (macroOrTransform instanceof Macro) {
                render((Macro) macroOrTransform, null, null, null);
            } else if (macroOrTransform instanceof TemplateTransformModel) {
                render(null, (TemplateTransformModel) macroOrTransform, null);
            } else {
                String nodeType = node.getNodeType();
                if (nodeType != null) {
                    // If the node's type is 'text', we just output it.
                    if ((nodeType.equals("text") && node instanceof TemplateScalarModel)) {
                        out.write(((TemplateScalarModel) node).getAsString());
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
                                        + nodeType + " either.", this);
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

    public void fallback() throws TemplateException, IOException {
        TemplateModel macroOrTransform = getNodeProcessor(currentNodeName,
                currentNodeNS, nodeNamespaceIndex);
        if (macroOrTransform instanceof Macro) {
            render((Macro) macroOrTransform, null, null, null);
        } else if (macroOrTransform instanceof TemplateTransformModel) {
            render(null, (TemplateTransformModel) macroOrTransform, null);
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
            MacroContext mc = new MacroContext(macro, this, nestedBlock,
                    bodyParameters);
            MacroContext prevMc = macroContextLookup.get(macro);
            macroContextLookup.put(macro, mc);
            if (args != null) {
                Map<String, TemplateModel> argsMap = macro.getParams().getParameterMap(args, this);
                for (Map.Entry<String, TemplateModel> entry : argsMap.entrySet()) {
                    mc.put(entry.getKey(), entry.getValue());
                }
            }
            Scope prevScope = currentScope;

            Configurable prevParent = getParent();
            currentScope = currentMacroContext = mc;
            try {
                mc.runMacro();
            } catch (ReturnException re) {
            } catch (TemplateException te) {
                handleTemplateException(te);
            } finally {
                if (prevMc != null) {
                    macroContextLookup.put(macro, prevMc);
                } else {
                    macroContextLookup.remove(macro);
                }
                currentMacroContext = mc.invokingMacroContext;
                currentScope = prevScope;
                setParent(prevParent);
            }
        } finally {
            popElement();
        }
    }

    public void visitMacroDef(Macro macro) {
        if (currentMacroContext == null) {
            macroToNamespaceLookup.put(macro, getCurrentNamespace());
            getCurrentNamespace().put(macro.getName(), macro);
        }
    }

    public TemplateNamespace getMacroNamespace(Macro macro) {
        TemplateNamespace result = macroToNamespaceLookup.get(macro);
        if (result == null) {
            result = mainNamespace; // REVISIT ??
        }
        return result;
    }
    
    public MacroContext getMacroContext(Macro macro) {
        return macroContextLookup.get(macro);
    }
    
    public void setCurriedMacroNamespace(Macro curriedMacro, Macro baseMacro) {
        TemplateNamespace tns = macroToNamespaceLookup.get(baseMacro);
        macroToNamespaceLookup.put(curriedMacro, tns);
    }

    public void process(TemplateNodeModel node, TemplateSequenceModel namespaces)
            throws TemplateException, IOException {
        if (node == null) {
            node = this.getCurrentVisitorNode();
            if (node == null) {
                throw new TemplateModelException(
                        "The target node of recursion is missing or null.");
            }
        }
        TemplateSequenceModel children = node.getChildNodes();
        if (children == null)
            return;
        for (int i = 0; i < children.size(); i++) {
            TemplateNodeModel child = (TemplateNodeModel) children.get(i);
            if (child != null) {
                render(child, namespaces);
            }
        }
    }

    public MacroContext getCurrentMacroContext() {
        return currentMacroContext;
    }

    private void handleTemplateException(TemplateException te)
            throws TemplateException {
        // Logic to prevent double-handling of the exception in
        // nested visit() calls.
        if (lastThrowable == te) {
            throw te;
        }
        lastThrowable = te;

        // Log the exception
        if (logger.isErrorEnabled()) {
            logger.error("", te);
        }

        // Stop exception is not passed to the handler, but
        // explicitly rethrown.
        if (te instanceof StopException) {
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
     * Note that altough it is not allowed to set this setting with the <tt>setting</tt>
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

    public String formatDate(Date date, int type) throws TemplateModelException {
        DateFormat df = getDateFormatObject(type);
        if (df == null) {
            throw new TemplateModelException(
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

    public TemplateModel getLastReturnValue() {
        return lastReturnValue;
    }

    public void setLastReturnValue(TemplateModel lastReturnValue) {
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

    public DateFormat getDateFormatObject(int dateType)
            throws TemplateModelException {
        switch (dateType) {
        case TemplateDateModel.UNKNOWN: {
            return null;
        }
        case TemplateDateModel.TIME: {
            if (timeFormat == null) {
                timeFormat = getDateFormatObject(dateType, getTimeFormat());
            }
            return timeFormat;
        }
        case TemplateDateModel.DATE: {
            if (dateFormat == null) {
                dateFormat = getDateFormatObject(dateType, getDateFormat());
            }
            return dateFormat;
        }
        case TemplateDateModel.DATETIME: {
            if (dateTimeFormat == null) {
                dateTimeFormat = getDateFormatObject(dateType,
                        getDateTimeFormat());
            }
            return dateTimeFormat;
        }
        default: {
            throw new TemplateModelException("Unrecognized date type "
                    + dateType);
        }
        }
    }

    public DateFormat getDateFormatObject(int dateType, String pattern)
            throws TemplateModelException {
        if (dateFormats == null) {
            dateFormats = new Map[4];
            dateFormats[TemplateDateModel.UNKNOWN] = new HashMap<String, DateFormat>();
            dateFormats[TemplateDateModel.TIME] = new HashMap<String, DateFormat>();
            dateFormats[TemplateDateModel.DATE] = new HashMap<String, DateFormat>();
            dateFormats[TemplateDateModel.DATETIME] = new HashMap<String, DateFormat>();
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
                    case TemplateDateModel.UNKNOWN: {
                        throw new TemplateModelException(
                                "Can't convert the date to string using a "
                                        + "built-in format, because it is not known which "
                                        + "parts of the date variable are in use. Use "
                                        + "?date, ?time or ?datetime built-in, or "
                                        + "?string.<format> or ?string(<format>) built-in "
                                        + "with explicit formatting pattern with this date.");
                    }
                    case TemplateDateModel.TIME: {
                        format = DateFormat.getTimeInstance(style, locale);
                        break;
                    }
                    case TemplateDateModel.DATE: {
                        format = DateFormat.getDateInstance(style, locale);
                        break;
                    }
                    case TemplateDateModel.DATETIME: {
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
                        throw new TemplateModelException("Can't parse "
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

    public TemplateTransformModel getTransform(Expression exp)
            throws TemplateException {
        TemplateTransformModel ttm = null;
        TemplateModel tm = exp.getAsTemplateModel(this);
        if (tm instanceof TemplateTransformModel) {
            ttm = (TemplateTransformModel) tm;
        } else if (exp instanceof Identifier) {
            tm = getConfiguration().getSharedVariable(exp.toString());
            if (tm instanceof TemplateTransformModel) {
                ttm = (TemplateTransformModel) tm;
            }
        }
        return ttm;
    }

    public TemplateModel resolveVariable(String name)
            throws TemplateModelException 
    {
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
    public TemplateModel getVariable(String name) throws TemplateModelException {
        return currentScope.resolveVariable(name);
    }

    /**
     * This method returns a variable from the "global" namespace and falls back
     * to the data model.
     */
    public TemplateModel get(String name) throws TemplateModelException {
        TemplateModel result = globalVariables.get(name);
        if (result == null) {
            result = rootDataModel.get(name);
        }
        if (result == null) {
        	result = getConfiguration().getSharedVariable(name);
        }
        return result;
    }

    public Collection<String> getDirectVariableNames() throws TemplateModelException {
        Collection<String> coll = new HashSet<String>(globalVariables.keySet());
        if (rootDataModel instanceof TemplateHashModelEx) {
            TemplateModelIterator rootNames =
                ((TemplateHashModelEx) rootDataModel).keys().iterator();
            while(rootNames.hasNext()) {
                coll.add(((TemplateScalarModel)rootNames.next()).getAsString());
            }
        }
        return coll;
    }

    /**
     * Sets a variable that is visible globally. This is correspondent to FTL
     * <code><#global <i>name</i>=<i>model</i>></code>.
     */
    public void setGlobalVariable(String name, TemplateModel model) {
        globalVariables.put(name, model);
    }

    /**
     * Sets a variable in the current namespace. This is correspondent to FTL
     * <code><#assign <i>name</i>=<i>model</i>></code>. This can be
     * considered a convenient shorthand for: getCurrentNamespace().put(name,
     * model)
     */
    public void setVariable(String name, TemplateModel model) {
        getCurrentNamespace().put(name, model);
    }

    /**
     * Sets a local variable (one effective only during a macro invocation).
     * This is correspondent to FTL
     * <code><#local <i>name</i>=<i>model</i>></code>.
     * 
     * @param name
     *            the identifier of the variable
     * @param model
     *            the value of the variable.
     * @throws IllegalStateException
     *             if the environment is not executing a macro body.
     */
    public void setLocalVariable(String name, TemplateModel model) {
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
     *            the identifier of the variable
     * @param model
     *            the value of the variable
     */
    public void unqualifiedSet(String name, TemplateModel model) throws TemplateModelException {
        Scope scope = this.currentScope;
        while (!(scope instanceof TemplateNamespace)) {
            if (scope.get(name) != null) {
                scope.put(name, model);
                return;
            }
            scope = scope.getEnclosingScope();
        }
        scope.put(name, model);
    }

    public Scope getCurrentScope() {
        return currentScope;
    }

    /**
     * Returns a set of variable names that are known at the time of call. This
     * includes names of all shared variables in the {@link Configuration},
     * names of all global variables that were assigned during the template
     * processing, names of all variables in the current name-space, names of
     * all local variables and loop variables. If the passed root data model
     * implements the {@link TemplateHashModelEx} interface, then all names it
     * retrieves through a call to {@link TemplateHashModelEx#keys()} method are
     * returned as well. The method returns a new Set object on each call that
     * is completely disconnected from the Environment. That is, modifying the
     * set will have no effect on the Environment object.
     */
    public Collection<String> getKnownVariableNames() throws TemplateModelException {
        Collection<String> coll = new HashSet<String>();
        Scope scope = currentScope;
        while(scope != null) {
            coll.addAll(scope.getDirectVariableNames());
            scope = scope.getEnclosingScope();
        }
        return coll;
    }

    /**
     * Outputs the instruction stack. Useful for debugging.
     * {@link TemplateException}s incorporate this information in their stack
     * traces.
     * @see #getElementStack() which exposes the actual element stack
     * so that you can write your own custom stack trace or error message
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
            pw.print(prev.getStartLocation());
            pw.println("]");
        }
        while (iter.hasPrevious()) {
            TemplateElement prev = iter.previous();
            if (prev instanceof UnifiedCall || prev instanceof Include) {
                String location = prev.getDescription() + " ["
                        + prev.getStartLocation() + "]";
                if (location != null && location.length() > 0) {
                    pw.print(" in ");
                    pw.println(location);
                }
            }
        }
        pw.println("----------");
        pw.flush();
    }

    /**
     * @return null This is the final fallback scope. It has no
     *         enclosing scope.
     */

    public Scope getEnclosingScope() {
        return null;
    }

    public boolean definesVariable(String name) {
        try {
            return globalVariables.containsKey(name)
                    || rootDataModel.get(name) != null;
        } catch (TemplateModelException tme) {
            return false;
        }
    }

    public void put(String varname, TemplateModel value) {
        globalVariables.put(varname, value);
    }

    public TemplateModel remove(String varname) {
        return globalVariables.remove(varname);
    }

    public boolean isEmpty() {
        return false; // REVISIT, is this right?
    }

    public int size() throws TemplateModelException {
        if (rootDataModel instanceof TemplateHashModelEx) {
            TemplateHashModelEx root = (TemplateHashModelEx) rootDataModel;
            return globalVariables.size() + root.size()
                    + getEnclosingScope().size();
        }
        throw new TemplateModelException(
                "The size() method is not applicable because the root data model does not expose a size() method.");
    }

    public TemplateCollectionModel keys() throws TemplateModelException {
        if (!(rootDataModel instanceof TemplateHashModelEx)) {
            throw new TemplateModelException(
                    "The keys() method is not applicable because the root data model does not expose a keys() method.");
        }
        TemplateHashModelEx root = (TemplateHashModelEx) rootDataModel;
        TemplateCollectionModel rootKeys = root.keys();
        TemplateCollectionModel sharedVariableKeys = getEnclosingScope().keys();
        LinkedHashSet<TemplateModel> aggregate = new LinkedHashSet<TemplateModel>();
        for (TemplateModelIterator tmi = sharedVariableKeys.iterator(); tmi
                .hasNext();) {
            aggregate.add(tmi.next());
        }
        for (TemplateModelIterator tmi = rootKeys.iterator(); tmi.hasNext();) {
            aggregate.add(tmi.next());
        }
        for (String varname : globalVariables.keySet()) {
            aggregate.add(new SimpleScalar(varname));
        }
        return new SimpleCollection(aggregate, getObjectWrapper());
    }

    public TemplateCollectionModel values() throws TemplateModelException {
        if (!(rootDataModel instanceof TemplateHashModelEx)) {
            throw new TemplateModelException(
                    "The keys() method is not applicable because the root data model does not expose a keys() method.");
        }
        TemplateHashModelEx root = (TemplateHashModelEx) rootDataModel;
        TemplateCollectionModel rootValues = root.values();
        TemplateCollectionModel sharedVariableValues = getEnclosingScope()
                .values();
        LinkedHashSet<TemplateModel> aggregate = new LinkedHashSet<TemplateModel>();
        for (TemplateModelIterator tmi = sharedVariableValues.iterator(); tmi
                .hasNext();) {
            aggregate.add(tmi.next());
        }
        for (TemplateModelIterator tmi = rootValues.iterator(); tmi.hasNext();) {
            aggregate.add(tmi.next());
        }
        for (TemplateModel value : globalVariables.values()) {
            aggregate.add(value);
        }
        return new SimpleCollection(aggregate, getObjectWrapper());
    }

    /**
     * Returns the name-space for the name if exists, or null.
     * 
     * @param name
     *            the template path that you have used with the
     *            <code>import</code> directive or
     *            {@link #importLib(String, String)} call, in normalized form.
     *            That is, the path must be an absolute path, and it must not
     *            contain "/../" or "/./". The leading "/" is optional.
     */
    public TemplateNamespace getNamespace(String name) {
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
    public TemplateNamespace getMainNamespace() {
        return mainNamespace;
    }

    /**
     * Returns the main name-space. This is correspondent of FTL
     * <code>.namespace</code> hash.
     */
    public TemplateNamespace getCurrentNamespace() {
        Scope scope = currentScope;
        while (!(scope instanceof TemplateNamespace)) {
            scope = scope.getEnclosingScope();
        }
        return (TemplateNamespace) scope;
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
    public TemplateHashModel getDataModel() {
    	final TemplateHashModel result = new TemplateHashModel() {
            public boolean isEmpty() {
                return false;
            }

            public TemplateModel get(String key) throws TemplateModelException {
                TemplateModel value = rootDataModel.get(key);
                if (value == null) {
                    value = getConfiguration().getSharedVariable(key);
                }
                return value;
            }
        };
        
        if (rootDataModel instanceof TemplateHashModelEx) {
        	return new TemplateHashModelEx() {
        		public boolean isEmpty() throws TemplateModelException {
        			return result.isEmpty();
        		}
        		public TemplateModel get(String key) throws TemplateModelException {
        			return result.get(key);
        		}
        		
        		//NB: The methods below do not take into account
        		// configuration shared variables even though
        		// the hash will return them, if only for BWC reasons
        		public TemplateCollectionModel values() throws TemplateModelException {
        			return ((TemplateHashModelEx) rootDataModel).values();
        		}
        		public TemplateCollectionModel keys() throws TemplateModelException {
        			return ((TemplateHashModelEx) rootDataModel).keys();
        		}
        		public int size() throws TemplateModelException {
        			return ((TemplateHashModelEx) rootDataModel).size();
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
    
    public TemplateNodeModel getCurrentVisitorNode() {
        return currentVisitorNode;
    }

    /**
     * sets TemplateNodeModel as the current visitor node.
     * <tt>.current_node</tt>
     */
    public void setCurrentVisitorNode(TemplateNodeModel node) {
        currentVisitorNode = node;
    }

    TemplateModel getNodeProcessor(TemplateNodeModel node)
            throws TemplateException {
        String nodeName = node.getNodeName();
        if (nodeName == null) {
            throw new TemplateException("Node name is null.", this);
        }
        TemplateModel result = getNodeProcessor(nodeName, node
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

    private TemplateModel getNodeProcessor(final String nodeName,
            final String nsURI, int startIndex) throws TemplateException {
        TemplateModel result = null;
        int i;
        for (i = startIndex; i < nodeNamespaces.size(); i++) {
            TemplateNamespace ns = null;
            try {
                ns = (TemplateNamespace) nodeNamespaces.get(i);
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

    /*
     * private TemplateModel getNodeProcessor(final String nodeName, final
     * String nsURI, int startIndex) throws TemplateException { TemplateModel
     * result = null; TemplateSequenceModel nodeNamespaces =
     * this.nodeNamespaces; if (currentMacroContext != null) { SimpleSequence ss =
     * new SimpleSequence(); Macro.Context ctxt = currentMacroContext; while
     * (ctxt != null) { ss.add(ctxt.getLocals()); ctxt =
     * ctxt.invokingMacroContext; } for (int i=0; i<this.nodeNamespaces.size();
     * i++) { ss.add(this.nodeNamespaces.get(i)); } nodeNamespaces = ss; } //
     * This is a bit tricky/hacky, if startIndex is -1 this is taken to mean //
     * that we are in a fallback from the inner macro to the outer namespace //
     * REVISIT: what about falling back to an enclosing macro first? /* if
     * (startIndex <0) { startIndex = 0; } else if (startIndex == 0 &&
     * currentMacroContext != null) { FTLNamespace ns =
     * currentMacroContext.getLocals(); result = getNodeProcessor(ns, nodeName,
     * nsURI); }
     */
    /*
     * int i; for (i = startIndex; i<nodeNamespaces.size(); i++) { FTLNamespace
     * ns = null; try { ns = (FTLNamespace) nodeNamespaces.get(i); } catch
     * (ClassCastException cce) { throw new InvalidReferenceException("A using
     * clause should contain a sequence of namespaces or strings that indicate
     * the location of importable macro libraries.", this); } result =
     * getNodeProcessor(ns, nodeName, nsURI); if (result != null) break; } if
     * (result != null) { this.nodeNamespaceIndex = i+1; this.currentNodeName =
     * nodeName; this.currentNodeNS = nsURI; } return result; }
     */

    private TemplateModel getNodeProcessor(TemplateNamespace ns,
            String localName, String nsURI) throws TemplateException {
        TemplateModel result = null;
        if (nsURI == null) {
            result = ns.get(localName);
            if (!(result instanceof Macro)
                    && !(result instanceof TemplateTransformModel)) {
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
                if (!(result instanceof Macro)
                        && !(result instanceof TemplateTransformModel)) {
                    result = null;
                }
            } else {
                if (nsURI.length() == 0) {
                    result = ns.get(Template.NO_NS_PREFIX + ":" + localName);
                    if (!(result instanceof Macro)
                            && !(result instanceof TemplateTransformModel)) {
                        result = null;
                    }
                }
                if (nsURI.equals(template.getDefaultNS())) {
                    result = ns.get(Template.DEFAULT_NAMESPACE_PREFIX + ":"
                            + localName);
                    if (!(result instanceof Macro)
                            && !(result instanceof TemplateTransformModel)) {
                        result = null;
                    }
                }
                if (result == null) {
                    result = ns.get(localName);
                    if (!(result instanceof Macro)
                            && !(result instanceof TemplateTransformModel)) {
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
     * @see #include(Template includedTemplate)
     */
    public void include(String name, String encoding, boolean parse)
            throws IOException, TemplateException {
        include(getTemplateForInclusion(name, encoding, parse));
    }

    /**
     * Gets a template for inclusion; used with
     * {@link #include(Template includedTemplate)}. The advantage over simply
     * using <code>config.getTemplate(...)</code> is that it chooses the
     * default encoding as the <code>include</code> directive does.
     * 
     * @param name
     *            the name of the template, relatively to the template root
     *            directory (not the to the directory of the currently executing
     *            template file!). (Note that you can use
     *            {@link freemarker.cache.TemplateCache#getFullTemplatePath} to
     *            convert paths to template root relative paths.)
     * @param encoding
     *            the encoding of the obtained template. If null, the encoding
     *            of the Template that is currently being processed in this
     *            Environment is used.
     * @param parse
     *            whether to process a parsed template or just include the
     *            unparsed template source.
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
     *            the template to process. Note that it does <em>not</em> need
     *            to be a template returned by
     *            {@link #getTemplateForInclusion(String name, String encoding, boolean parse)}.
     */
    public void include(Template includedTemplate) throws TemplateException,
            IOException {
        Template prevTemplate = getTemplate();
        setParent(includedTemplate);
        importMacros(includedTemplate);
        try {
            renderSecurely(includedTemplate.getRootElement(), 
                    includedTemplate.getCodeSource());
        } finally {
            setParent(prevTemplate);
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
     * @see #importLib(Template includedTemplate, String namespace)
     */
    public TemplateNamespace importLib(String name, String namespace)
            throws IOException, TemplateException {
        return importLib(getTemplateForImporting(name), namespace);
    }

    /**
     * Gets a template for importing; used with
     * {@link #importLib(Template importedTemplate, String namespace)}. The
     * advantage over simply using <code>config.getTemplate(...)</code> is
     * that it chooses the encoding as the <code>import</code> directive does.
     * 
     * @param name
     *            the name of the template, relatively to the template root
     *            directory (not the to the directory of the currently executing
     *            template file!). (Note that you can use
     *            {@link freemarker.cache.TemplateCache#getFullTemplatePath} to
     *            convert paths to template root relative paths.)
     */
    public Template getTemplateForImporting(String name) throws IOException {
        return getTemplateForInclusion(name, null, true);
    }

    /**
     * Emulates <code>import</code> directive.
     * 
     * @param loadedTemplate
     *            the template to import. Note that it does <em>not</em> need
     *            to be a template returned by
     *            {@link #getTemplateForImporting(String name)}.
     */
    public TemplateNamespace importLib(Template loadedTemplate, String namespace)
            throws IOException, TemplateException {
        if (loadedLibs == null) {
            loadedLibs = new HashMap<String, TemplateNamespace>();
        }
        String templateName = loadedTemplate.getName();
        TemplateNamespace existingNamespace = loadedLibs.get(templateName);
        if (existingNamespace != null) {
            if (namespace != null) {
                setVariable(namespace, existingNamespace);
            }
        } else {
            TemplateNamespace newNamespace = new TemplateNamespace(this,
                    loadedTemplate);
            if (namespace != null) {
                setVariable(namespace, newNamespace);
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
            this.out = NULL_WRITER;
            try {
                include(loadedTemplate);
            } finally {
                this.out = prevOut;
                currentScope = prevScope;
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
        for (Iterator it = ((TemplateCore)template).getMacrosNoCheck().values().iterator(); it
                .hasNext();) {
            visitMacroDef((Macro) it.next());
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

    /**
     * A hook that Jython uses.
     */
    public Object __getitem__(String key) throws TemplateModelException {
        return BeansWrapper.getDefaultInstance().unwrap(getVariable(key));
    }

    /**
     * A hook that Jython uses.
     */
    public void __setitem__(String key, Object o) throws TemplateException {
        setGlobalVariable(key, getObjectWrapper().wrap(o));
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
