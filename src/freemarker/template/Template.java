package freemarker.template;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

import freemarker.core.Configurable;
import freemarker.core.Environment;
import freemarker.core.TemplateCore;
import freemarker.core.ast.ASTVisitor;
import freemarker.core.parser.ast.ImportDeclaration;
import freemarker.core.parser.ast.TemplateElement;
import freemarker.core.ast.TemplateHeaderElement;
import freemarker.core.parser.*;
import freemarker.ext.beans.SimpleMapModel;

import static freemarker.ext.beans.ObjectWrapper.wrap;
/**
 * <p>A core FreeMarker API that represents a compiled template.
 * Typically, you will use a {@link Configuration} object to instantiate a template.
 *
 * <PRE>
      Configuration cfg = new Configuration();
      ...
      Template myTemplate = cfg.getTemplate("myTemplate.html");
   </PRE>
 *
 * <P>However, you can also construct a template directly by passing in to
 * the appropriate constructor a java.io.Reader instance that is set to
 * read the raw template text. The compiled template is
 * stored in an an efficient data structure for later use.
 *
 * <p>To render the template, i.e. to merge it with a data model, and
 * thus produce "cooked" output, call the <tt>process</tt> method.
 *
 * <p>Any error messages from exceptions thrown during compilation will be
 * included in the output stream and thrown back to the calling code.
 * To change this behavior, you can install custom exception handlers using
 * {@link Configurable#setTemplateExceptionHandler(TemplateExceptionHandler)} on
 * a Configuration object (for all templates belonging to a configuration) or on
 * a Template object (for a single template).
 * 
 * <p>It's not legal to modify the values of FreeMarker settings: a) while the
 * template is executing; b) if the template object is already accessible from
 * multiple threads.
 * 
 * @version $Id: Template.java,v 1.218 2005/12/07 00:31:18 revusky Exp $
 */

public class Template extends TemplateCore {
    public static final String DEFAULT_NAMESPACE_PREFIX = "D";
    public static final String NO_NS_PREFIX = "N";

    private List<ImportDeclaration> imports = new Vector<>();
    private String encoding, defaultNS;
    private final String name;
    BitSet leftTrimLines = new BitSet();
    BitSet rightTrimLines = new BitSet();
    BitSet noTrimLines = new BitSet();
    BitSet outputtingLines = new BitSet();
    BitSet outputtingLinesInMacro = new BitSet();

    private Map<String, String> prefixToNamespaceURILookup = new HashMap<String, String>();
    private Map<String, String> namespaceURIToPrefixLookup = new HashMap<String, String>();
    private Set<String> declaredVariables = new HashSet<String>();
    
    boolean stripWhitespace;
    private boolean strictVariableDeclaration;
    
    private List<ParsingProblem> parsingProblems = new ArrayList<>();
    private TemplateHeaderElement headerElement;
    
    /**
     * A prime constructor to which all other constructors should
     * delegate directly or indirectly.
     */
    protected Template(String name, Configuration cfg)
    {
        super(cfg != null ? cfg : Configuration.getDefaultConfiguration());
        this.name = name;
    }

    /**
     * Constructs a template from a character stream.
     *
     * @param name the path of the template file relative to the directory what you use to store
     *        the templates. See {@link #getName} for more details.
     * @param reader the character stream to read from. It will always be closed (Reader.close()).
     * @param cfg the Configuration object that this Template is associated with.
     *        If this is null, the "default" {@link Configuration} object is used,
     *        which is highly discouraged, because it can easily lead to
     *        erroneous, unpredictable behaviour.
     *        (See more {@link Configuration#getDefaultConfiguration() here...})
     * @param encoding This is the encoding that we are supposed to be using. If this is
     * non-null (It's not actually necessary because we are using a Reader) then it is
     * checked against the encoding specified in the FTL header -- assuming that is specified,
     * and if they don't match a WrongEncodingException is thrown.
     */
	public Template(String name, Reader reader, Configuration cfg,
			String encoding) throws IOException 
    {
        this(name, cfg);
        this.encoding = encoding;
        try {
            int syntaxSetting = getConfiguration().getTagSyntax();
            this.stripWhitespace = getConfiguration().getWhitespaceStripping();
            this.strictVariableDeclaration = getConfiguration().getStrictVariableDefinition();
            CharSequence content = readInTemplateText(reader);
            FMParser parser = new FMParser(this, content, syntaxSetting);
            parser.setInputSource(getName());
            setRootElement(parser.Root());
            PostParseVisitor ppv = new PostParseVisitor(this);
            ppv.visit(this);
            if (stripWhitespace) {
                WhitespaceAdjuster wadj = new WhitespaceAdjuster(this);
                wadj.visit(this);
            }
            for (ASTVisitor visitor : cfg.getAutoVisitors()) {
            	if (visitor instanceof Cloneable) {
            		visitor = visitor.clone();
            	}
            	visitor.visit(this);
            }
        }
        catch(ParseException e) {
            e.setTemplateName(name);
            throw e;
        }
        namespaceURIToPrefixLookup = Collections.unmodifiableMap(namespaceURIToPrefixLookup);
        prefixToNamespaceURILookup = Collections.unmodifiableMap(prefixToNamespaceURILookup);
	}

	public Template(String name, CharSequence input, Configuration cfg,
			String encoding) throws IOException 
    {
        this(name, cfg);
        this.encoding = encoding;
        
        //readInTemplateText(reader);
        try {
            int syntaxSetting = getConfiguration().getTagSyntax();
            this.stripWhitespace = getConfiguration().getWhitespaceStripping();
            this.strictVariableDeclaration = getConfiguration().getStrictVariableDefinition();
            FMParser parser = new FMParser(this, input, syntaxSetting);
            parser.setInputSource(getName());
            setRootElement(parser.Root());
            PostParseVisitor ppv = new PostParseVisitor(this);
            ppv.visit(this);
            WhitespaceAdjuster wadj = new WhitespaceAdjuster(this);
            wadj.visit(this);
            for (ASTVisitor visitor : cfg.getAutoVisitors()) {
            	if (visitor instanceof Cloneable) {
            		visitor = visitor.clone();
            	}
            	visitor.visit(this);
            }
        }
        catch(ParseException e) {
            e.setTemplateName(name);
            throw e;
        }
        namespaceURIToPrefixLookup = Collections.unmodifiableMap(namespaceURIToPrefixLookup);
        prefixToNamespaceURILookup = Collections.unmodifiableMap(prefixToNamespaceURILookup);
	}

    
	
	private CharSequence readInTemplateText(Reader reader) throws IOException {
        int charsRead = 0;
        StringBuilder buf = new StringBuilder();
        char[] chars = new char[0x10000];
        try {
        	do {
        		charsRead = reader.read(chars);
        		if (charsRead >0) buf.append(chars, 0, charsRead);
        	} while(charsRead >=0);
        }
        finally {
        	reader.close();
        }
        return buf;
//        char[] result = new char[buf.length()];
//        buf.getChars(0, buf.length(), result, 0);
//        return result;
	}    
    
    /**
     * This is equivalent to Template(name, reader, cfg, null)
     */
    public Template(String name, Reader reader, Configuration cfg) throws IOException {
        this(name, reader, cfg, null);
    }


    /**
     * Constructs a template from a character stream.
     *
     * This is the same as the 3 parameter version when you pass null
     * as the cfg parameter.
     * 
     * @deprecated This constructor uses the "default" {@link Configuration}
     * instance, which can easily lead to erroneous, unpredictable behaviuour.
     * See more {@link Configuration#getDefaultConfiguration() here...}.
     */
    public Template(String name, Reader reader) throws IOException {
        this(name, reader, null);
    }

    /**
     * Returns a trivial template, one that is just a single block of
     * plain text, no dynamic content. (Used by the cache module to create
     * unparsed templates.)
     * @param name the path of the template file relative to the directory what you use to store
     *        the templates. See {@link #getName} for more details.
     * @param content the block of text that this template represents
     * @param config the configuration to which this template belongs
     */
    static public Template getPlainTextTemplate(String name, String content, 
            Configuration config) {
        Template template = new Template(name, config);
        template.setRootElement(new TemplateElement() {
        	public void execute(Environment env) throws IOException {
        		env.getOut().write(content);
        	}
        });
        return template;
    }
    
    public void setStripWhitespace(boolean stripWhitespace) {
    	this.stripWhitespace = stripWhitespace;
    }

    /**
     * Processes the template, using data from the map, and outputs
     * the resulting text to the supplied <tt>Writer</tt> The elements of the
     * map are converted to template models using the default object wrapper
     * returned by the {@link Configuration#getObjectWrapper() getObjectWrapper()}
     * method of the <tt>Configuration</tt>.
     * @param rootMap the root node of the data model.  
     * @param out a <tt>Writer</tt> to output the text to.
     * @throws TemplateException if an exception occurs during template processing
     * @throws IOException if an I/O exception occurs during writing to the writer.
     */
    public void process(Object rootMap, Writer out) throws IOException
    {
        createProcessingEnvironment(rootMap, out).process();
    }

    /**
     * Processes the template, using data from the root map object, and outputs
     * the resulting text to the supplied writer, using the supplied
     * object wrapper to convert map elements to template models.
     * @param rootMap the root node of the data model.  If null, an
     * empty data model is used. Can be any object that the effective object
     * wrapper can turn into a <tt>TemplateHashModel</tt> Basically, simple and
     * beans wrapper can turn <tt>java.util.Map</tt> objects into hashes.
     * Naturally, you can pass any object directly implementing
     * <tt>TemplateHashModel</tt> as well.
     * @param out the writer to output the text to.
     * @param rootNode The root node for recursive processing, this may be null.
     * 
     * @throws TemplateException if an exception occurs during template processing
     * @throws IOException if an I/O exception occurs during writing to the writer.
     */
    public void process(Object rootMap, Writer out, TemplateNodeModel rootNode)
    throws TemplateException, IOException
    {
        Environment env = createProcessingEnvironment(rootMap, out);
        if (rootNode != null) {
            env.setCurrentVisitorNode(rootNode);
        }
        env.process();
    }
    
   /**
    * Creates a {@link freemarker.core.Environment Environment} object,
    * using this template, the data model provided as the root map object, and
    * the supplied object wrapper to convert map elements to template models.
    * You can then call Environment.process() on the returned environment
    * to set off the actual rendering.
    * Use this method if you want to do some special initialization on the environment
    * before template processing, or if you want to read the environment after template
    * processing.
    *
    * <p>Example:
    *
    * <p>This:
    * <pre>
    * Environment env = myTemplate.createProcessingEnvironment(root, out, null);
    * env.process();
    * </pre>
    * is equivalent with this:
    * <pre>
    * myTemplate.process(root, out);
    * </pre>
    * But with <tt>createProcessingEnvironment</tt>, you can manipulate the environment
    * before and after the processing:
    * <pre>
    * Environment env = myTemplate.createProcessingEnvironment(root, out);
    * env.include("include/common.ftl", null, true);  // before processing
    * env.process();
    * TemplateModel x = env.getVariable("x");  // after processing
    * </pre>
    *
    * @param rootMap the root node of the data model.  If null, an
    * empty data model is used. Can be any object that the effective object
    * wrapper can turn into a <tt>TemplateHashModel</tt> Basically, simple and
    * beans wrapper can turn <tt>java.util.Map</tt> objects into hashes.
    * Naturally, you can pass any object directly implementing
    * <tt>TemplateHashModel</tt> as well.
    * @param out the writer to output the text to.
    * @return the {@link freemarker.core.Environment Environment} object created for processing
    * @throws TemplateException if an exception occurs while setting up the Environment object.
    */
    public Environment createProcessingEnvironment(Object rootMap, Writer out)
    throws TemplateException
    {
        return new Environment(this, (TemplateHashModel) wrap(rootMap), out);
    }

    /**
     * The path of the template file relative to the directory what you use to store the templates.
     * For example, if the real path of template is <tt>"/www/templates/community/forum.fm"</tt>,
     * and you use "<tt>"/www/templates"</tt> as
     * {@link Configuration#setDirectoryForTemplateLoading "directoryForTemplateLoading"},
     * then <tt>name</tt> should be <tt>"community/forum.fm"</tt>. The <tt>name</tt> is used for example when you
     * use <tt>&lt;include ...></tt> and you give a path that is relative to the current
     * template, or in error messages when FreeMarker logs an error while it processes the template.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the Configuration object associated with this template.
     */
    public Configuration getConfiguration() {
        return (Configuration) getFallback();
    }
    
    public List<ParsingProblem> getParsingProblems() {
    	return parsingProblems;
    }
    
    public boolean hasParsingProblems() {
    	return !parsingProblems.isEmpty();
    }
    
    public void addParsingProblem(ParsingProblem problem) {
    	parsingProblems.add(problem);
    }
    
    /**
     * Sets the character encoding to use for
     * included files. Usually you don't set this value manually,
     * instead it is assigned to the template upon loading.
     */

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns the character encoding used for reading included files.
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Called by code internally to maintain
     * a list of imports
     */
    public void addImport(ImportDeclaration id) {
        imports.add(id);
    }
    
    public void setHeaderElement(TemplateHeaderElement headerElement) {
    	this.headerElement = headerElement;
    }
    
    public TemplateHeaderElement getHeaderElement() {
    	return headerElement;
    }
    
    public boolean declaresVariable(String name) {
    	return declaredVariables.contains(name);
    }
    
    public Set<String> getDeclaredVariables() {
    	return Collections.unmodifiableSet(declaredVariables);
    }
    
    public void declareVariable(String name) {
    	if (declaredVariables == null) declaredVariables = new HashSet<String>();
    	declaredVariables.add(name);
    }
    
    public boolean strictVariableDeclaration() {
    	return strictVariableDeclaration;
    }
    
    public void setStrictVariableDeclaration(boolean strictVariableDeclaration) {
    	this.strictVariableDeclaration = strictVariableDeclaration;
    }

    public void setLineSaysLeftTrim(int i) {
        leftTrimLines.set(i);
    }
    
    public void setLineSaysRightTrim(int i) {
        rightTrimLines.set(i);
    }
    
    public void setLineSaysTrim(int i) {
        leftTrimLines.set(i);
        rightTrimLines.set(i);
    }
    
    public void setLineSaysNoTrim(int i) {
        noTrimLines.set(i);
    }
    
    public boolean lineSaysLeftTrim(int i) {
        return leftTrimLines.get(i);
    }
    
    public boolean lineSaysRightTrim(int i) {
        return rightTrimLines.get(i);
    }
    
    public boolean lineSaysNoTrim(int i) {
        return noTrimLines.get(i);
    }
    
    public void markAsOutputtingLine(int lineNumber, boolean inMacro) {
    	if (inMacro) {
            outputtingLinesInMacro.set(lineNumber);
    	} else {
            outputtingLines.set(lineNumber);
    	}
    }
    
    public boolean isOutputtingLine(int i, boolean inMacro) {
        return inMacro ? outputtingLinesInMacro.get(i) : outputtingLines.get(i);
    }
    
    /**
     *  @return the root TemplateElement object.
     */
    public TemplateElement getRootTreeNode() {
        return getRootElement();
    }

    public List<ImportDeclaration> getImports() {
        return imports;
    }

    /**
     * This is used internally.
     */
    public void addPrefixNSMapping(String prefix, String nsURI) {
        if (nsURI.length() == 0) {
            throw new IllegalArgumentException("Cannot map empty string URI");
        }
        if (prefix.length() == 0) {
            throw new IllegalArgumentException("Cannot map empty string prefix");
        }
        if (prefix.equals(NO_NS_PREFIX)) {
            throw new IllegalArgumentException("The prefix: " + prefix + " cannot be registered, it is reserved for special internal use.");
        }
        if (prefixToNamespaceURILookup.containsKey(prefix)) {
            throw new IllegalArgumentException("The prefix: '" + prefix + "' was repeated. This is illegal.");
        }
        if (namespaceURIToPrefixLookup.containsKey(nsURI)) {
            throw new IllegalArgumentException("The namespace URI: " + nsURI + " cannot be mapped to 2 different prefixes.");
        }
        if (prefix.equals(DEFAULT_NAMESPACE_PREFIX)) {
            this.defaultNS = nsURI;
        } else {
            prefixToNamespaceURILookup.put(prefix, nsURI);
            namespaceURIToPrefixLookup.put(nsURI, prefix);
        }
    }
    
    public String getDefaultNS() {
        return this.defaultNS;
    }
    
    /**
     * @return the NamespaceUri mapped to this prefix in this template. (Or null if there is none.)
     */
    public String getNamespaceForPrefix(String prefix) {
        if (prefix.equals("")) {
            return defaultNS == null ? "" : defaultNS;
        }
        return prefixToNamespaceURILookup.get(prefix);
    }
    
    /**
     * @return the prefix mapped to this nsURI in this template. (Or null if there is none.)
     */
    public String getPrefixForNamespace(String nsURI) {
        if (nsURI == null) {
            return null;
        }
        if (nsURI.length() == 0) {
            return defaultNS == null ? "" : NO_NS_PREFIX;
        }
        if (nsURI.equals(defaultNS)) {
            return "";
        }
        return namespaceURIToPrefixLookup.get(nsURI);
    }
    
    static public class WrongEncodingException extends RuntimeException {

        public String specifiedEncoding;

        public WrongEncodingException(String specifiedEncoding) {
            this.specifiedEncoding = specifiedEncoding;
        }

    }
}

