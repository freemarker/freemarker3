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

package freemarker.template;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.*;

import javax.swing.tree.TreePath;

import freemarker.core.Configurable;
import freemarker.core.Environment;
import freemarker.core.TemplateCore;
import freemarker.core.ast.ASTVisitor;
import freemarker.core.ast.LibraryLoad;
import freemarker.core.ast.TemplateElement;
import freemarker.core.ast.TemplateHeaderElement;
import freemarker.core.ast.TextBlock;
import freemarker.core.parser.*;
import freemarker.debug.impl.DebuggerService;

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
    public static final CodeSource NULL_CODE_SOURCE = new CodeSource(null, 
            (Certificate[])null);
    
    public static final String DEFAULT_NAMESPACE_PREFIX = "D";
    public static final String NO_NS_PREFIX = "N";

	protected char[] templateText;
    private List<LibraryLoad> imports = new Vector<LibraryLoad>();
    private String encoding, defaultNS;
    private final String name;
	private int[] lineStartOffsets;
	private byte[] lineInfoTable; // contain bitsets that describe what happens
	                              // on the line.
	
	
    
    private Map<String, String> prefixToNamespaceURILookup = new HashMap<String, String>();
    private Map<String, String> namespaceURIToPrefixLookup = new HashMap<String, String>();
    private Set<String> declaredVariables = new HashSet<String>();
    
    //This is necessary for backward compatibility
    private Set<String> implicitlyDeclaredVariables = new HashSet<String>();
    private final CodeSource codeSource;
    boolean stripWhitespace;
    private boolean strictVariableDeclaration;
    
    private List<ParsingProblem> parsingProblems = new ArrayList<ParsingProblem>();
    private TemplateHeaderElement headerElement;

    
    /**
     * A prime constructor to which all other constructors should
     * delegate directly or indirectly.
     */
    protected Template(String name, Configuration cfg, CodeSource codeSource)
    {
        super(cfg != null ? cfg : Configuration.getDefaultConfiguration());
        this.name = name;
        this.codeSource = codeSource == null ? NULL_CODE_SOURCE : codeSource;
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
     * @param codeSource the code source used to determine the security 
     * privileges for the template based on the Java policy in effect when it 
     * is run within a secured environment. Can be null, which is treated as 
     * being equivalent to untrusted code.
     */
    
    
	public Template(String name, Reader reader, Configuration cfg,
			String encoding, CodeSource codeSource) throws IOException {
       this(name, cfg, codeSource);
        
        this.encoding = encoding;
        
        readInTemplateText(reader);
        try {
            try {
                int syntaxSetting = getConfiguration().getTagSyntax();
                this.stripWhitespace = getConfiguration().getWhitespaceStripping();
                this.strictVariableDeclaration = getConfiguration().getStrictVariableDefinition();
                FMParser parser = new FMParser(this, new CharArrayReader(templateText), syntaxSetting);
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
            catch (TokenMgrError exc) {
                throw new ParseException("Token manager error: " + exc, 0, 0);
            }
        }
        catch(ParseException e) {
            e.setTemplateName(name);
            throw e;
        }
        DebuggerService.registerTemplate(this);
        namespaceURIToPrefixLookup = Collections.unmodifiableMap(namespaceURIToPrefixLookup);
        prefixToNamespaceURILookup = Collections.unmodifiableMap(prefixToNamespaceURILookup);
	}
	
	protected void readInTemplateText(Reader reader) throws IOException {
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
        this.templateText = new char[buf.length()];
        buf.getChars(0, buf.length(), templateText, 0);
        this.lineStartOffsets = createLineTable(templateText);
        this.lineInfoTable = new byte[lineStartOffsets.length];
	}    
    
    public Template(String name, Reader reader, Configuration cfg, 
            String encoding)
    throws IOException
    {
        this(name, reader, cfg, encoding, NULL_CODE_SOURCE);
    }
    
    /**
     * This is equivalent to Template(name, reader, cfg, null)
     */
    public Template(String name, Reader reader, Configuration cfg) throws IOException {
        this(name, reader, cfg, null, NULL_CODE_SOURCE);
    }


    /**
     * Constructs a template from a character stream.
     *
     * This is the same as the 3 parameter version when you pass null
     * as the cfg parameter.
     * 
     * @deprecated This constructor uses the "default" {@link Configuration}
     * instance, which can easily lead to erroneous, unpredictable behaviour.
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
        Template template = new Template(name, config, NULL_CODE_SOURCE);
        TextBlock block = new TextBlock(content, true);
        template.templateText = content.toCharArray();
        template.setRootElement(block);
        DebuggerService.registerTemplate(template);
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
     * @param rootMap the root node of the data model.  If null, an
     * empty data model is used. Can be any object that the effective object
     * wrapper can turn into a <tt>TemplateHashModel</tt>. Basically, simple and
     * beans wrapper can turn <tt>java.util.Map</tt> objects into hashes
     * and the Jython wrapper can turn both a <tt>PyDictionary</tt> as well as
     * any object that implements <tt>__getitem__</tt> into a template hash.
     * Naturally, you can pass any object directly implementing
     * <tt>TemplateHashModel</tt> as well.
     * @param out a <tt>Writer</tt> to output the text to.
     * @throws TemplateException if an exception occurs during template processing
     * @throws IOException if an I/O exception occurs during writing to the writer.
     */
    public void process(Object rootMap, Writer out)
    throws TemplateException, IOException
    {
        createProcessingEnvironment(rootMap, out, null).process();
    }

    /**
     * Processes the template, using data from the root map object, and outputs
     * the resulting text to the supplied writer, using the supplied
     * object wrapper to convert map elements to template models.
     * @param rootMap the root node of the data model.  If null, an
     * empty data model is used. Can be any object that the effective object
     * wrapper can turn into a <tt>TemplateHashModel</tt> Basically, simple and
     * beans wrapper can turn <tt>java.util.Map</tt> objects into hashes
     * and the Jython wrapper can turn both a <tt>PyDictionary</tt> as well as any
     * object that implements <tt>__getitem__</tt> into a template hash.
     * Naturally, you can pass any object directly implementing
     * <tt>TemplateHashModel</tt> as well.
     * @param wrapper The object wrapper to use to wrap objects into
     * {@link TemplateModel} instances. If null, the default wrapper retrieved
     * by {@link Configurable#getObjectWrapper()} is used.
     * @param out the writer to output the text to.
     * @param rootNode The root node for recursive processing, this may be null.
     * 
     * @throws TemplateException if an exception occurs during template processing
     * @throws IOException if an I/O exception occurs during writing to the writer.
     */
    public void process(Object rootMap, Writer out, ObjectWrapper wrapper, TemplateNodeModel rootNode)
    throws TemplateException, IOException
    {
        Environment env = createProcessingEnvironment(rootMap, out, wrapper);
        if (rootNode != null) {
            env.setCurrentVisitorNode(rootNode);
        }
        env.process();
    }
    
    /**
     * Processes the template, using data from the root map object, and outputs
     * the resulting text to the supplied writer, using the supplied
     * object wrapper to convert map elements to template models.
     * @param rootMap the root node of the data model.  If null, an
     * empty data model is used. Can be any object that the effective object
     * wrapper can turn into a <tt>TemplateHashModel</tt> Basically, simple and
     * beans wrapper can turn <tt>java.util.Map</tt> objects into hashes
     * and the Jython wrapper can turn both a <tt>PyDictionary</tt> as well as any
     * object that implements <tt>__getitem__</tt> into a template hash.
     * Naturally, you can pass any object directly implementing
     * <tt>TemplateHashModel</tt> as well.
     * @param wrapper The object wrapper to use to wrap objects into
     * {@link TemplateModel} instances. If null, the default wrapper retrieved
     * by {@link Configurable#getObjectWrapper()} is used.
     * @param out the writer to output the text to.
     * 
     * @throws TemplateException if an exception occurs during template processing
     * @throws IOException if an I/O exception occurs during writing to the writer.
     */
    public void process(Object rootMap, Writer out, ObjectWrapper wrapper)
    throws TemplateException, IOException
    {
        process(rootMap, out, wrapper, null);
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
    * beans wrapper can turn <tt>java.util.Map</tt> objects into hashes
    * and the Jython wrapper can turn both a <tt>PyDictionary</tt> as well as any
    * object that implements <tt>__getitem__</tt> into a template hash.
    * Naturally, you can pass any object directly implementing
    * <tt>TemplateHashModel</tt> as well.
    * @param wrapper The object wrapper to use to wrap objects into
    * {@link TemplateModel} instances. If null, the default wrapper retrieved
    * by {@link Configurable#getObjectWrapper()} is used.
    * @param out the writer to output the text to.
    * @return the {@link freemarker.core.Environment Environment} object created for processing
    * @throws TemplateException if an exception occurs while setting up the Environment object.
    * @throws IOException if an exception occurs doing any auto-imports
    */
    public Environment createProcessingEnvironment(Object rootMap, Writer out, ObjectWrapper wrapper)
    throws TemplateException, IOException
    {
        TemplateHashModel root = null;
        if(rootMap instanceof TemplateHashModel) {
            root = (TemplateHashModel)rootMap;
        }
        else {
            if(wrapper == null) {
                wrapper = getObjectWrapper();
            }

            try {
                root = rootMap != null
                    ? (TemplateHashModel)wrapper.wrap(rootMap)
                    : new SimpleHash(wrapper);
                if(root == null) {
                    throw new IllegalArgumentException(wrapper.getClass().getName() + " converted " + (rootMap == null ? "null" : rootMap.getClass().getName()) + " to null.");
                }
            }
            catch(ClassCastException e) {
                throw new IllegalArgumentException(wrapper.getClass().getName() + " could not convert " + (rootMap == null ? "null" : rootMap.getClass().getName()) + " to a TemplateHashModel.");
            }
        }
        Environment env = new Environment(this, root, out);
        getConfiguration().doAutoImports(env);
        getConfiguration().doAutoIncludes(env);
        return env;
    }

    /**
     * Same as <code>createProcessingEnvironment(rootMap, out, null)</code>.
     * @see #createProcessingEnvironment(Object rootMap, Writer out, ObjectWrapper wrapper)
     */
    public Environment createProcessingEnvironment(Object rootMap, Writer out)
    throws TemplateException, IOException
    {
        return createProcessingEnvironment(rootMap, out, null);
    }
    
    /**
     * Returns a string representing the raw template
     * text in canonical form.
     */
    public String toString() {
        StringWriter sw = new StringWriter();
        try {
            dump(sw);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }
        return sw.toString();
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
        return (Configuration) getParent();
    }
    
    public List<ParsingProblem> getParsingProblems() {
    	return parsingProblems;
    }
    
    public boolean hasParsingProblems() {
    	return !parsingProblems.isEmpty();
    }
    
    void addParsingProblem(ParsingProblem problem) {
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
    public void addImport(LibraryLoad ll) {
        imports.add(ll);
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
    
    /**
     * Used internally, this is a complication necessary for
     * backward compatibility in includes.
     */
    public void setImplicitlyDeclaredVariables(Set<String> names) {
    	implicitlyDeclaredVariables = names;
    }
    
    /**
     * only used internally, it says whether the variable
     * was implicitly declared, that means not in a #var
     * statement but either via a legacy #assign or as the 
     * name of a macro or as the namespace name in an #import
     * This is rather tricky and only necessary for backward
     * compatibility with older style #include's 
     */
    public boolean isImplicitlyDeclared(String varname) {
    	return implicitlyDeclaredVariables.contains(varname);
    }
    
    public boolean strictVariableDeclaration() {
    	return strictVariableDeclaration;
    }
    
   public void setStrictVariableDeclaration(boolean strictVariableDeclaration) {
    	this.strictVariableDeclaration = strictVariableDeclaration;
    }
   
   	public String getSource(int beginColumn,
           int beginLine,
           int endColumn,
           int endLine)
   	{
   		// Our container is zero-based, but location info is 1-based
   		--beginLine;
   		--beginColumn;
   		--endColumn;
   		--endLine;
   		int startOffset = lineStartOffsets[beginLine] + beginColumn;
   		int endOffset = lineStartOffsets[endLine] + endColumn;
   		int numChars = 1+endOffset - startOffset;
   		return new String(templateText, startOffset, numChars);
   	}
   
    public String getLine(int lineNumber) {
    	lineNumber--;
    	int lineStartOffset = lineStartOffsets[lineNumber];
    	int numChars;
    	if (lineNumber == lineStartOffsets.length-1) {
    		numChars = templateText.length - lineStartOffset;
    	} else {
    		numChars = lineStartOffsets[lineNumber+1] - lineStartOffset;
    	}
     	return new String(templateText, lineStartOffset, numChars);
    }
    
    public int getTabAdjustedColumn(int lineNumber, int column, int tabSize) {
    	if (tabSize == 1 || column == 1) return column;
    	int lineStartOffset = lineStartOffsets[lineNumber-1];
    	int result = 1;
    	char c=0;
    	for (int i=0; i< column-1; i++) {
    		c = templateText[lineStartOffset+i];
    		if (c == '\t') 
    			result += (tabSize - i%tabSize);
    		else ++result;
    	}
    	return result;
    }
   
   
    public void writeTextAt(Writer out, 
    					  int beginColumn,
    					  int beginLine, 
    					  int endColumn, 
		                  int endLine) throws IOException 
    {
    	--beginLine; --beginColumn; --endColumn; --endLine;
   		int startOffset = lineStartOffsets[beginLine] + beginColumn;
   		int endOffset = lineStartOffsets[endLine] + endColumn;
   		out.write(templateText, startOffset, 1+endOffset - startOffset);
    }
    
    public void writeTemplateText(Writer out) throws IOException {
    	out.write(templateText);
    }
    
    static private int countLines(char[] chars) {
     	if (chars == null || chars.length == 0) return 0;
     	int numLines = 1;
        for (int i=0; i<chars.length; i++) {
         	boolean isLastChar = (i == chars.length-1);
         	if (chars[i] =='\r') {
         		if (!isLastChar && chars[i+1] != '\n') ++numLines;
         	}
         	else if (chars[i] == '\n') {
         		if (!isLastChar) ++numLines;
         	}
         }
         return numLines;
    }
     
    static private int[] createLineTable(final char[] text) {
     	int numLines = countLines(text);
     	int[] table = new int[numLines];
     	int lineNumber = 0;
     	boolean newLine = true;
     	for (int i=0; i<text.length; i++) {
     		if (newLine) table[lineNumber++] = i;
     		newLine = false;
     		if (text[i] == '\r') {
     			newLine = (i != text.length -1 && text[i+1] !='\n');
     		}
     		else if (text[i] == '\n') {
     			newLine = true;
     		}
     	}
     	return table;
    }
    
    public void setLineSaysLeftTrim(int i) {
    	--i;
    	lineInfoTable[i] = (byte) (lineInfoTable[i] | 0x01);
    }
    
    public void setLineSaysRightTrim(int i) {
    	--i;
    	lineInfoTable[i] = (byte) (lineInfoTable[i] | 0x02);
    }
    
    public void setLineSaysTrim(int i) {
    	--i;
    	lineInfoTable[i] = (byte) (lineInfoTable[i] | 0x03);
    }
    
    public void setLineSaysNoTrim(int i) {
    	--i;
    	lineInfoTable[i] = (byte) (lineInfoTable[i] | 0x04);
    }
    
    public boolean lineSaysLeftTrim(int i) {
    	--i;
    	return (lineInfoTable[i] & 1) != 0;
    }
    
    public boolean lineSaysRightTrim(int i) {
    	--i;
    	return (lineInfoTable[i] & 2) != 0;
    }
    
    public boolean lineSaysNoTrim(int i) {
    	--i;
    	return (lineInfoTable[i] & 4) != 0;
    }
    
    public void markAsOutputtingLine(int lineNumber, boolean inMacro) {
    	--lineNumber;
    	int bitMask = inMacro ? 0x10 : 0x08;
    	if (inMacro) {
    		lineInfoTable[lineNumber] = (byte) (lineInfoTable[lineNumber] | bitMask);
    		
    	} else {
    		lineInfoTable[lineNumber] = (byte) (lineInfoTable[lineNumber] | bitMask);
    	}
    }
    
    public boolean isOutputtingLine(int i, boolean inMacro) {
    	--i;
    	int bitMask = inMacro ? 0x10 : 0x08;
    	return (lineInfoTable[i] & bitMask) != 0;
    }
    
    
     

    
    /**
     * Returns the template source at the location
     * specified by the coordinates given.
     * @param beginColumn the first column of the requested source, 1-based
     * @param beginLine the first line of the requested source, 1-based
     * @param endColumn the last column of the requested source, 1-based
     * @param endLine the last line of the requested source, 1-based
     * @see freemarker.core.ast.TemplateNode#getSource()
     */

    /**
     *  @return the root TemplateElement object.
     *  @throws SecurityException if the getConfiguration().isSecure()
     *  returns true, there is a security manager in the JVM, and the caller
     *  of this method does not posess the "modifyTemplate" FreeMarker 
     *  permission (since the retrieved root tree node is mutable).
     */
    public TemplateElement getRootTreeNode() {
        checkModifyTemplate();
        return getRootElement();
    }

    /**
     *  @throws SecurityException if the getConfiguration().isSecureTemplates()
     *  returns true, there is a security manager in the JVM, and the caller
     *  of this method does not posess the "modifyTemplate" FreeMarker 
     *  permission.
     */
    @Override
    public void setParent(Configurable parent) {
        checkModifyTemplate();
        super.setParent(parent);
    }

    public List<LibraryLoad> getImports() {
        return imports;
    }

    public CodeSource getCodeSource() {
        return codeSource;
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
    
    /**
     * @return the prefixed name, based on the ns_prefixes defined
     * in this template's header for the local name and node namespace
     * passed in as parameters.
     */
    public String getPrefixedName(String localName, String nsURI) {
        if (nsURI == null || nsURI.length() == 0) {
            if (defaultNS != null) {
                return NO_NS_PREFIX + ":" + localName;
            } else {
                return localName;
            }
        } 
        if (nsURI.equals(defaultNS)) {
            return localName;
        } 
        String prefix = getPrefixForNamespace(nsURI);
        if (prefix == null) {
            return null;
        }
        return prefix + ":" + localName;
    }
    
    /**
     * @return an array of the elements containing the given column and line numbers.
     * @param column the column
     * @param line the line
     * @throws SecurityException if the getConfiguration().isSecureTemplates()
     * returns true, there is a security manager in the JVM, and the caller
     * of this method does not posess the "modifyTemplate" FreeMarker 
     * permission.
     */
    public TreePath containingElements(int column, int line) {
        checkModifyTemplate();
        ArrayList<TemplateElement> elements = new ArrayList<TemplateElement>();
        TemplateElement element = getRootElement();
mainloop:
        while (element.contains(column, line)) {
            elements.add(element);
            for (Enumeration enumeration = element.children(); enumeration.hasMoreElements();) {
                TemplateElement elem = (TemplateElement) enumeration.nextElement();
                if (elem.contains(column, line)) {
                    element = elem;
                    continue mainloop;
                }
            }
            break;
        }
        if (elements.isEmpty()) {
            return null;
        }
        return new TreePath(elements.toArray());
    }

    static public class WrongEncodingException extends ParseException {
        private static final long serialVersionUID = 3716984277969927605L;

        public String specifiedEncoding;

        public WrongEncodingException(String specifiedEncoding) {
            this.specifiedEncoding = specifiedEncoding;
        }

    }
}

