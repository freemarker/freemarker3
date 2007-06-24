import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.ext.dom.NodeModel;
import freemarker.template.utility.*;
import org.w3c.dom.*;
import org.xml.sax.*;
//import org.apache.xml.resolver.CatalogManager;
//import org.apache.xml.resolver.tools.CatalogResolver;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.*;
import java.io.*;
import java.text.Collator;

public class Transform {
    public static final String PROP_OUTPUT_DIR = "outputDir";
    public static final String PROP_TEMPLATE_DIR = "templateDir";
    public static final String PROP_SRC_FILE = "srcFile";
    public static final String PROP_SHOW_EDITOR_NOTES = "showEditorNotes";
    public static final String PROP_STOP_ON_VALIDATION_ERROR = "stopOnValidationError";
    public static final String PROP_OUTPUT_WARNINGS = "outputWarnings";
        
    /*//*
     * The name of the optional property whose value is the absolute paths of
     * XML catalog files separated by semicolons. If this is not specified or
     * not properly specified, the XML parser will load the DTD files from
     * the location specified by system identifier given in the DOCTYPE.
     * As that's usually a HTTP URL, it means that for each single XML parsing
     * you will have to wait until it downloards the DTD, furthermore the
     * parsing will fail if you are offline.
     */
    //public static final String PROP_CATALOG_FILES = "catalogFiles";
    
    static File outputDir;
    static Configuration fmConfig = Configuration.getDefaultConfiguration();
        
    static Map olinks = new HashMap();
    
    static Map nodesToOutputFiles,
        idToNodeHash=new HashMap(),
        primaryIndexTermLookup = new HashMap(),
        secondaryIndexTermLookup = new HashMap(),
        xrefLabelLookup = new HashMap();
    static List outputNodes = new ArrayList(),
        filenames = new ArrayList(),
        fileElements = new ArrayList(),
        indexEntries;
    static Set idAttrElements = new HashSet();  // elements that should have an id attribute
    static Integer appendixNumber = new Integer(0);
    static Integer chapterNumber = new Integer(0);
    static Integer partNumber = new Integer(0);
    static boolean stopOnValidationError, outputWarnings;
    static int outputNodeIndex;
    
    static {
        olinks.put("api", "api/index.html");
        olinks.put("homepage", "http://freemarker.org/");

        fileElements.add("appendix");
        fileElements.add("book");
        fileElements.add("chapter");
        fileElements.add("glossary");
        fileElements.add("index");
        fileElements.add("part");
        fileElements.add("preface");
        fileElements.add("sect1");

        idAttrElements.add("part");
        idAttrElements.add("chapter");
        idAttrElements.add("sect1");
        idAttrElements.add("sect2");
        idAttrElements.add("sect3");
        idAttrElements.add("simplesect");
        idAttrElements.add("glossentry");
    }
    
    static public void main(String[] args) throws Exception {
        Properties properties = new Properties();
        InputStream is = Transform.class.getResourceAsStream("transform.properties");
        if (is == null) {
            System.err.println("Must have a transform.properties file that specifies "
                    + PROP_TEMPLATE_DIR + ", " + PROP_OUTPUT_DIR + ", and " + PROP_SRC_FILE + ".");
        } else {
            properties.load(is);
            is.close();
        }
        if (!properties.containsKey(PROP_OUTPUT_DIR)
            || !properties.containsKey(PROP_TEMPLATE_DIR)
            || !properties.containsKey(PROP_SRC_FILE))
        {
            System.err.println("Must specify " + PROP_TEMPLATE_DIR + ", "
                    + PROP_OUTPUT_DIR + ", and "
                    + PROP_SRC_FILE + " in transform.properties.");
            System.exit(-1);
        }
        startTransformation(properties);
    }
    
    static void startTransformation(Properties properties) throws Exception {
        File srcFile = new File(properties.getProperty(PROP_SRC_FILE));
        File templateDir = new File(properties.getProperty(PROP_TEMPLATE_DIR));
        outputDir = new File(properties.getProperty(PROP_OUTPUT_DIR));
        String showNotes = properties.getProperty(PROP_SHOW_EDITOR_NOTES, "false");
        TemplateBooleanModel showEditorNotes = StringUtil.getYesNo(showNotes)
                ? TemplateBooleanModel.TRUE
                : TemplateBooleanModel.FALSE;
        stopOnValidationError = StringUtil.getYesNo(
                properties.getProperty(PROP_STOP_ON_VALIDATION_ERROR, "true"));
        outputWarnings = StringUtil.getYesNo(properties.getProperty(PROP_OUTPUT_WARNINGS, "false"));
        fmConfig.setSharedVariable(PROP_SHOW_EDITOR_NOTES, showEditorNotes);
        // fmConfig.setSharedVariable("colorize_ftl", new FreeMarkerToHtml(false));
        fmConfig.setDirectoryForTemplateLoading(templateDir);

        fmConfig.setLocale(Locale.US);
        fmConfig.setTimeZone(TimeZone.getTimeZone("GMT"));
        fmConfig.setSharedVariable("transformStartTime", new Date());
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        //db.setEntityResolver(createCatalogResolver(properties.getProperty(PROP_CATALOG_FILES)));
        db.setErrorHandler(errorHandler);
        Document docNode = db.parse(srcFile);
        NodeModel.simplify(docNode);
        
        addIdAttributes(docNode);
        createLookupTables(docNode);
        indexEntries = new ArrayList(primaryIndexTermLookup.keySet());
        Collections.sort(indexEntries, Collator.getInstance(Locale.US));
        System.out.println("Outputting " + outputNodes.size() + " files to: " + outputDir.getAbsolutePath());
        for (outputNodeIndex =0; outputNodeIndex<outputNodes.size(); outputNodeIndex++) {
            Transform tr = new Transform((Element) outputNodes.get(outputNodeIndex));
            tr.process();
        }
    }
    
    
    /**
     * Adds attribute <tt>id</tt> to elements that are in <code>idAttrElements</code>,
     * but has no id attribute yet.
     * Adding id-s is useful to create more precise HTML cross-links later.
     */
    static void addIdAttributes(Node node) {
        addIdAttributes(node, new int[1]);
    }
    
    static void addIdAttributes(Node node, int[] id) {
        if (node instanceof Element) {
            String name = node.getNodeName();
            if (idAttrElements.contains(name)) {
                Element e = (Element) node;
                if (!e.hasAttribute("id")) {
                    id[0]++;
                    e.setAttribute("id", "autoid_" + id[0]);
                }
                if ("para".equals(name)) {
                    return;
                }
            }
        }
        NodeList children = node.getChildNodes();
        int ln = children.getLength();
        for (int i = 0; i < ln; i++) {
            addIdAttributes(children.item(i), id);
        }
    }
    
    static void createLookupTables(Node node) {
        if (node instanceof Element) {
            String id = ((Element) node).getAttribute("id");
            String xrefLabel = ((Element) node).getAttribute("xreflabel");
            String nodeName = node.getNodeName();
            if (id != null && id.length() >0) {
                idToNodeHash.put(id, node);
                if (xrefLabel != null && xrefLabel.length() >0) {
                    xrefLabelLookup.put(id, xrefLabel);
                }
            }
            if (nodeName.equals("indexterm")) {
                indexTerm(node);
            }
            else if (fileElements.contains(nodeName)) {
                outputNodes.add(node);
                filenames.add(getOutputFile(node));
            }
        }
        NodeList children = node.getChildNodes();
        for (int i=0; i < children.getLength(); i++) {
            createLookupTables(children.item(i));
        }
    }
    
    
    static void indexTerm(Node node) {
        Node primary = null;
        Node secondary = null;
        
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equals("primary")) {
                    primary = child;
                }
                else if (child.getNodeName().equals("secondary")) {
                    secondary = child;
                }
            }
        }
    
        String primaryText = primary.getFirstChild().getNodeValue().trim();
        if (!primaryIndexTermLookup.containsKey(primaryText)) {
            primaryIndexTermLookup.put(primaryText, new ArrayList());
        }
    
        if (secondary != null) {
            if (!secondaryIndexTermLookup.containsKey(primaryText)) {
                secondaryIndexTermLookup.put(primaryText, new TreeMap());
            }
            Map m = (Map) secondaryIndexTermLookup.get(primaryText);
            String secondaryText = secondary.getFirstChild().getNodeValue().trim();
            List nodes = (List) m.get(secondaryText);
            if (nodes == null) {
                nodes = new ArrayList();
                m.put(secondaryText, nodes);
            }
            nodes.add(NodeModel.wrap(node));
        }
        else {
            ((List) primaryIndexTermLookup.get(primaryText)).add(NodeModel.wrap(node));
        }
    }
    
    Element outputNode;
    String currentFilename, previousFilename, nextFilename, parentFilename;
    Environment env;
    SimpleHash dataModel = new SimpleHash();
    
    Transform(Element outputNode) {
        this.outputNode = outputNode;
    }
    
    void process() throws IOException, TemplateException {
        setFilenames();
        exposeVariables();
        Template template = fmConfig.getTemplate(outputNode.getNodeName() + ".ftl");
        File outputFile = new File(outputDir, currentFilename);
//        System.out.println("outputting: " + outputFile);
	FileOutputStream fos = new FileOutputStream(outputFile);
	OutputStreamWriter osw = new OutputStreamWriter(fos, "ISO-8859-1");
//        Writer writer = new BufferedWriter(new FileWriter(outputFile), 2048);
        Writer writer = new BufferedWriter(osw, 2048);
        try {
            template.process(dataModel, writer, null, NodeModel.wrap(outputNode));
        }
        finally {
            writer.close();
        }
    }
    
    Node getPreviousElement(Node node) {
        Node prev = outputNode.getPreviousSibling();
        while (prev != null && prev.getNodeType() != Node.ELEMENT_NODE) {
            prev = prev.getPreviousSibling();
        }
        if (prev == null) {
            prev = outputNode.getParentNode();
        }
        return prev;
    }
    
    Node getNextElement(Node node) {
        NodeList children = node.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE)
                return child;
        }
        Node next = node.getNextSibling();
        while (next != null) {
            next = next.getNextSibling();
            if (next instanceof Element)
                return next;
        }
        return null;
    }
    
    void setFilenames() {
        currentFilename = (String) filenames.get(outputNodeIndex);
        nextFilename = (outputNodeIndex < outputNodes.size() -1) ?
                        (String) filenames.get(1+outputNodeIndex) :
                        "";
        previousFilename = (outputNodeIndex >0) ?
                          (String) filenames.get(outputNodeIndex -1) :
                          "";
        if (outputNode.getParentNode() instanceof Element) {
            parentFilename = getOutputFile(outputNode.getParentNode());
        } else {
            parentFilename = "";
        }
        if (outputNode.getNodeName().equals("appendix")) {
            appendixNumber = new Integer(appendixNumber.intValue() +1);
        }
        else if (outputNode.getNodeName().equals("chapter")) {
            chapterNumber = new Integer(chapterNumber.intValue() + 1);
        }
        else if (outputNode.getNodeName().equals("part")) {
            chapterNumber = new Integer(0);
            partNumber = new Integer(partNumber.intValue() + 1);
        }
    }
    
    
    void exposeVariables() {
        dataModel.put("NodeFromID", NodeFromID);
        dataModel.put("CreateLinkFromID", CreateLinkFromID);
        dataModel.put("primaryIndexTermLookup", primaryIndexTermLookup);
        dataModel.put("secondaryIndexTermLookup", secondaryIndexTermLookup);
        dataModel.put("xrefLabelLookup", xrefLabelLookup);
        dataModel.put("CreateLinkFromNode", CreateLinkFromNode);
        dataModel.put("previousFilename", previousFilename);
        dataModel.put("nextFilename", nextFilename);
        dataModel.put("currentFilename", currentFilename);
        dataModel.put("parentFilename", parentFilename);
        dataModel.put("olinks", olinks);
        dataModel.put("indexEntries", indexEntries);
        dataModel.put("appendixNumber", appendixNumber);
        dataModel.put("chapterNumber", chapterNumber);
        dataModel.put("partNumber", partNumber);
    }
    
    
    TemplateMethodModel CreateLinkFromID = new TemplateMethodModel() {
        public Object exec(List args) throws TemplateModelException {
            if (args.size() != 1) {
                throw new TemplateModelException("Method CreateLinkFromID should have exactly one parameter");
            }
            String id = (String) args.get(0);
            Node node = (Node) idToNodeHash.get(id);
            String link = getOutputFile(node);
            if (link.equals(currentFilename))
                link = "";
            if (!outputNodes.contains(node)) {
                link = link + "#" + id;
            }
            return new SimpleScalar(link);
        }
    };
    
    TemplateMethodModel CreateLinkFromNode = new TemplateMethodModelEx() {
        public Object exec(List args) throws TemplateModelException {
            if (args.size() != 1) {
                throw new TemplateModelException("Method CreateLinkFromNode should have exactly one parameter");
            }
            Node node = ((NodeModel) args.get(0)).getNode();
            String id = getID(node);
            node = (Node) idToNodeHash.get(id);
            String link = getOutputFile(node);
            if (link.equals(currentFilename)) {
                link = "";
            }
            if (!outputNodes.contains(node)) {
                link = link + "#" + id;
            }
            return new SimpleScalar(link);
        }
        
        String getID(Node node) {
            while (node != null) {
                if (node instanceof Element) {
                    String att = ((Element) node).getAttribute("id");
                    if (att != null && att.length() >0)
                        return att;
                }
                node = node.getParentNode();
            }
            return null;
        }
    };
    
    static ErrorHandler errorHandler = new ErrorHandler() {
        public void warning(SAXParseException spe) {
            if (outputWarnings)
                System.err.println("warning: " + spe.getMessage());
        }
        
        public void error(SAXParseException spe) throws SAXParseException {
            System.err.println("error: " + spe.getMessage());
            int line = spe.getLineNumber();
            int column = spe.getColumnNumber();
            String sysID = spe.getSystemId();
            System.err.println("on line " + line + ", column " + column + " of " + sysID);
            if (stopOnValidationError)
                throw spe;
        }

        public void fatalError(SAXParseException spe) throws SAXParseException {
            System.err.println("fatal error: " + spe.getMessage());
            System.err.println("error: " + spe.getMessage());
            int line = spe.getLineNumber();
            int column = spe.getColumnNumber();
            String sysID = spe.getSystemId();
            System.err.println("on line " + line + ", column " + column + " of " + sysID);
            throw spe;
        }
    };
    
    TemplateMethodModel NodeFromID = new TemplateMethodModel() {
        public Object exec(List args) throws TemplateModelException {
            Node node = (Node) idToNodeHash.get(args.get(0));
            return NodeModel.wrap(node);
        }
    };
    
    static String getOutputFile(Node node) {
        if (node == null)
            return "";
        if (outputNodes.contains(node)) {
            String id = ((Element) node).getAttribute("id");
            if (id == null ||id.length() == 0)
                id = "index";
            return id + ".html";
        }
        return getOutputFile(node.getParentNode());
    }

    /*
    public static CatalogResolver createCatalogResolver(String catalogs) {
        CatalogManager cm = new CatalogManager();
        cm.setIgnoreMissingProperties(true);
        //cm.setVerbosity(9);
        cm.setUseStaticCatalog(false);
        if (catalogs != null && catalogs.length() != 0) {
            StringBuffer sb;
            Vector v = cm.getCatalogFiles();
            if (v != null) {
                sb = new StringBuffer();
                for (int i = 0; i < v.size(); i++) {
                    if (i != 0) {
                        sb.append(';');
                    }
                    sb.append((String) v.get(i));
                }
            } else {
                sb = null;
            }
            if (sb != null && sb.length() != 0) {
                cm.setCatalogFiles(catalogs + ";" + sb);
            } else {
                cm.setCatalogFiles(catalogs);
            }
        }
        return new CatalogResolver(cm);  
    }
    */
    
}
