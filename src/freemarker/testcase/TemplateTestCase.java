/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package freemarker.testcase;

import freemarker.template.*;
import freemarker.ext.beans.*;
import freemarker.ext.dom.NodeModel;
import freemarker.testcase.models.*;
import freemarker.template.utility.*;
import junit.framework.*;
import java.util.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;


public class TemplateTestCase extends TestCase {
    
    Template template;
    HashMap<String,Object> dataModel = new HashMap<String, Object>();
    
    String filename, testName;
    String inputDir = "template";
    String referenceDir = "reference";
    File outputDir;
    
    Configuration conf = new Configuration();
    
    public TemplateTestCase(String name, String filename) {
        super(name);
        this.filename = filename;
        this.testName = name;
        conf.setLocale(new Locale("en", "US"));
    }
    
    public void setTemplateDirectory(String dirname, File baseDir) throws IOException {
        File dir = new File(baseDir, dirname);
        conf.setDirectoryForTemplateLoading(dir);
        System.out.println("Setting loading directory as: " + dir);
    }

    public void setOutputDirectory(String dirname, File baseDir) {
        this.outputDir = new File(baseDir, dirname);
        System.out.println("Setting reference directory as: " + outputDir);
    }

    public void setConfigParam(String param, String value, File baseDir) throws IOException {
        if ("templatedir".equals(param)) {
            setTemplateDirectory(value, baseDir);
        }
        else if ("auto_import".equals(param)) {
            StringTokenizer st = new StringTokenizer(value);
            if (!st.hasMoreTokens()) fail("Expecting libname");
            String libname = st.nextToken();
            if (!st.hasMoreTokens()) fail("Expecting 'as <alias>' in autoimport");
            String as = st.nextToken();
            if (!as.equals("as")) fail("Expecting 'as <alias>' in autoimport");
            if (!st.hasMoreTokens()) fail("Expecting alias after 'as' in autoimport");
            String alias = st.nextToken();
            conf.addAutoImport(alias, libname);
        }
        else if ("clear_encoding_map".equals(param)) {
            if (StringUtil.getYesNo(value)) {
                conf.clearEncodingMap();
            }
        }
        else if ("input_encoding".equals(param)) {
            conf.setDefaultEncoding(value);
        }
        else if ("outputdir".equals(param)) {
            setOutputDirectory(value, baseDir);
        }
        else if ("output_encoding".equals(param)) {
            conf.setOutputEncoding(value);
        }
        else if ("locale".equals(param)) {
            String lang = "", country="", variant="";
            StringTokenizer st = new StringTokenizer(value,"_", false);
            if (st.hasMoreTokens()) {
                lang = st.nextToken();
            }
            if (st.hasMoreTokens()) {
                country = st.nextToken();
            }
            if (st.hasMoreTokens()){
                variant = st.nextToken();
            }
            if (lang != "") {
                Locale loc = new Locale(lang, country, variant);
                conf.setLocale(loc);
            }
        }
        else if ("object_wrapper".equals(param)) {
            try {
                Class cl = Class.forName(value);
                ObjectWrapper ow = (ObjectWrapper) cl.newInstance();
                conf.setObjectWrapper(ow);
            } catch (Exception e) {
                fail("Error setting object wrapper to " + value + "\n" + e.getMessage());
            }
        }
        else if ("input_encoding".equals(param)) {
            conf.setDefaultEncoding(value);
        }
        else if ("output_encoding".equals(param)) {
            conf.setOutputEncoding(value);
        }
        else if ("url_escaping_charset".equals(param)) {
            conf.setURLEscapingCharset(value);
        }
    }
    
    /*
     * This method just contains all the code to seed the data model 
     * ported over from the individual classes. This seems ugly and unnecessary.
     * We really might as well just expose pretty much 
     * the same tree to all our tests. (JR)
     */
    
    public void setUp() throws Exception {
        dataModel.put("message", "Hello, world!");
        
        if (testName.equals("bean-maps")) {
            BeansWrapper w1 = new BeansWrapper();
            BeansWrapper w2 = new BeansWrapper();
            BeansWrapper w3 = new BeansWrapper();
            BeansWrapper w4 = new BeansWrapper();
            BeansWrapper w5 = new BeansWrapper();
            BeansWrapper w6 = new BeansWrapper();
            BeansWrapper w7 = new BeansWrapper();
            w1.setSimpleMapWrapper(false);
            w2.setSimpleMapWrapper(false);
            w3.setSimpleMapWrapper(false);
            w4.setSimpleMapWrapper(false);
            w5.setSimpleMapWrapper(false);
            w6.setSimpleMapWrapper(false);
            w7.setSimpleMapWrapper(false);
    
            w1.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
            w2.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
            w3.setExposureLevel(BeansWrapper.EXPOSE_NOTHING);
            w4.setExposureLevel(BeansWrapper.EXPOSE_NOTHING);
            w5.setExposureLevel(BeansWrapper.EXPOSE_ALL);
            w6.setExposureLevel(BeansWrapper.EXPOSE_ALL);
    
            w1.setMethodsShadowItems(true);
            w2.setMethodsShadowItems(false);
            w3.setMethodsShadowItems(true);
            w4.setMethodsShadowItems(false);
            w5.setMethodsShadowItems(true);
            w6.setMethodsShadowItems(false);
    
            w7.setSimpleMapWrapper(true);
    
            Object test = getTestBean();
    
            dataModel.put("m1", w1.wrap(test));
            dataModel.put("m2", w2.wrap(test));
            dataModel.put("m3", w3.wrap(test));
            dataModel.put("m4", w4.wrap(test));
            dataModel.put("m5", w5.wrap(test));
            dataModel.put("m6", w6.wrap(test));
            dataModel.put("m7", w7.wrap(test));
    
            dataModel.put("s1", w1.wrap("hello"));
            dataModel.put("s2", w1.wrap("world"));
            dataModel.put("s3", w5.wrap("hello"));
            dataModel.put("s4", w5.wrap("world"));
        }
        
        else if (testName.equals("beans")) {
            dataModel.put("array", new String[] { "array-0", "array-1"});
            dataModel.put("list", Arrays.asList(new String[] { "list-0", "list-1", "list-2"}));
            Map tmap = new HashMap();
            tmap.put("key", "value");
            Object objKey = new Object();
            tmap.put(objKey, "objValue");
            dataModel.put("map", tmap);
            dataModel.put("objKey", objKey);
            dataModel.put("obj", new freemarker.testcase.models.BeanTestClass());
            dataModel.put("resourceBundle", new ResourceBundleModel(ResourceBundle.getBundle("freemarker.testcase.models.BeansTestResources"), BeansWrapper.getDefaultInstance()));
            dataModel.put("date", new GregorianCalendar(1974, 10, 14).getTime());
            dataModel.put("statics", BeansWrapper.getDefaultInstance().getStaticModels());
            dataModel.put("enums", BeansWrapper.getDefaultInstance().getEnumModels());
        }
        
        else if (testName.equals("boolean")) {
            dataModel.put( "boolean1", TemplateBooleanModel.FALSE);
            dataModel.put( "boolean2", TemplateBooleanModel.TRUE);
            dataModel.put( "boolean3", TemplateBooleanModel.TRUE);
            dataModel.put( "boolean4", TemplateBooleanModel.TRUE);
            dataModel.put( "boolean5", TemplateBooleanModel.FALSE);
            
            dataModel.put( "list1", new BooleanList1() );
            dataModel.put( "list2", new BooleanList2() );
    
            dataModel.put( "hash1", new BooleanHash1() );
            dataModel.put( "hash2", new BooleanHash2() );
        }
        
        else if (testName.equals("dateformat")) {
            GregorianCalendar cal = new GregorianCalendar(2002, 10, 15, 14, 54, 13);
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            dataModel.put("date", new SimpleDate(cal.getTime(), TemplateDateModel.DATETIME));
            dataModel.put("unknownDate", new SimpleDate(cal.getTime(), TemplateDateModel.UNKNOWN));
        }
    
        else if (testName.equals("number-format")) {
            dataModel.put("int", new SimpleNumber(Integer.valueOf(1)));
            dataModel.put("double", new SimpleNumber(Double.valueOf(1.0)));
            dataModel.put("double2", new SimpleNumber(Double.valueOf(1 + 1e-15)));
            dataModel.put("double3", new SimpleNumber(Double.valueOf(1e-16)));
            dataModel.put("double4", new SimpleNumber(Double.valueOf(-1e-16)));
            dataModel.put("bigDecimal", new SimpleNumber(java.math.BigDecimal.valueOf(1)));
            dataModel.put("bigDecimal2", new SimpleNumber(java.math.BigDecimal.valueOf(1, 16)));
        }

        else if (testName.equals("default-xmlns")) {
            InputSource is = new InputSource(getClass().getResourceAsStream("test-defaultxmlns1.xml"));
            NodeModel nm = NodeModel.parse(is);
            dataModel.put("doc", nm);
        }
        
        else if (testName.equals("multimodels")) {
            dataModel.put("test", "selftest");
            dataModel.put("self", "self");
            dataModel.put("zero", new Integer(0));
            dataModel.put("data", new MultiModel1());
        }
/*        
        else if (testName.equals("nodelistmodel")) {
            org.jdom.Document doc = new SAXBuilder().build(new InputSource(getClass().getResourceAsStream("test-xml.xml")));
            dataModel.put("doc", new NodeListModel(doc));
        }
*/        
        
        else if (testName.equals("string-builtins3")) {
            dataModel.put("multi", new TestBoolean());
        }
        
        else if (testName.equals("type-builtins")) {
            dataModel.put("testmethod", new TestMethod());
            dataModel.put("testnode", new TestNode());
            dataModel.put("testcollection", new SimpleCollection(new ArrayList()));
        }
        
        else if (testName.equals("var-layers")) {
            dataModel.put("x", new Integer(4));
            dataModel.put("z", new Integer(4));
            conf.setSharedVariable("y", new Integer(7));
        }
        
        else if (testName.equals("xml-fragment")) {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            DocumentBuilder db = f.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(new InputSource(getClass().getResourceAsStream("test-xmlfragment.xml")));
            dataModel.put("node", NodeModel.wrap(doc.getDocumentElement().getFirstChild().getFirstChild()));
        }
        
        else if (testName.equals("xmlns1")) {
            InputSource is = new InputSource(getClass().getResourceAsStream("test-xmlns.xml"));
            NodeModel nm = NodeModel.parse(is);
            dataModel.put("doc", nm);
        }
        
        else if (testName.equals("xmlns2")) {
            InputSource is = new InputSource(getClass().getResourceAsStream("test-xmlns2.xml"));
            NodeModel nm = NodeModel.parse(is);
            dataModel.put("doc", nm);
        }
        
        else if (testName.equals("xmlns3") || testName.equals("xmlns4")) {
            InputSource is = new InputSource(getClass().getResourceAsStream("test-xmlns3.xml"));
            NodeModel nm = NodeModel.parse(is);
            dataModel.put("doc", nm);
        }
        
        else if (testName.equals("xmlns5")) {
            InputSource is = new InputSource(getClass().getResourceAsStream("test-defaultxmlns1.xml"));
            NodeModel nm = NodeModel.parse(is);
            dataModel.put("doc", nm);
        }
        else if (testName.equals("xpath1")) {
            InputSource is = new InputSource(getClass().getResourceAsStream("test-xpath1.xml"));
            NodeModel nm = NodeModel.parse(is);
            dataModel.put("doc", nm);
        }
    }
    
    public void runTest() {
        try {
            template = conf.getTemplate(filename);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            fail("Could not load template " + filename + "\n" + sw.toString());
        }
        File refFile = new File (outputDir, filename);
        File outFile = new File (outputDir, filename+".out");
        Writer out = null;
        String encoding = conf.getOutputEncoding();
        if (encoding == null) encoding = "UTF-8";
        try {
            out = new OutputStreamWriter(new FileOutputStream(outFile), 
                    encoding);
        } catch (IOException ioe) {
            fail("Cannot write to file: " + outFile + "\n" + ioe.getMessage());
        }
        try {
            template.process(dataModel, out);
            out.close();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            fail("Could not process template " + filename + "\n" + sw.toString());
        }
        try {
            Reader ref = new InputStreamReader(new FileInputStream(refFile), 
                    encoding);
            Reader output = new InputStreamReader(new FileInputStream(outFile), 
                    encoding);
            compare(ref, output, refFile, outFile);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            fail("Error comparing files " + refFile + " and " + outFile + "\n" + sw.toString());
        }
        outFile.delete();
    }

    static public void compare(Reader reference, Reader output, 
            File refFile, File outFile) throws IOException
    {
        LineNumberReader ref = new LineNumberReader(reference);
        LineNumberReader out = new LineNumberReader(output);
        String refLine = "", outLine = "";
        Writer sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        boolean fail = false;
        while (refLine != null || outLine != null) {
            if (refLine == null) {
                pw.println("Output text is longer than reference text");
                fail = true;
                break;
            }
            if (outLine == null) {
                pw.println("Output text is shorter than reference text");
                fail = true;
                break;
            }
            refLine = ref.readLine();
            outLine = out.readLine();
            if (refLine != null && outLine != null & !refLine.equals(outLine)) {
                fail = true;
                pw.println("Difference found on line " + ref.getLineNumber() + 
                                            ".\nReference text is: " + refLine +
                                            "\nOutput text is   : " + outLine);
            }
        }
        if(fail) {
            pw.println("Reference file: " + refFile);
            pw.println("Output file: " + outFile);
            pw.flush();
            fail(sw.toString());
        }
    }
    
    static class TestBoolean implements TemplateBooleanModel, TemplateScalarModel {
        public boolean getAsBoolean() {
            return true;
        }
        
        public String getAsString() {
            return "de";
        }
    }
    @Parameters("foo")    
    static class TestMethod implements TemplateMethodModel {
      public Object exec(java.util.List arguments) {
          return "Parameter foo is: " + arguments.get(0);
      }
    }
    
    static class TestNode implements TemplateNodeModel {
      
      public String getNodeName() {
          return "name";
      }
                    
      public TemplateNodeModel getParentNode() {
          return null;
      }
    
      public String getNodeType() {
          return "element";
      }
    
      public TemplateSequenceModel getChildNodes() {
          return null;
      }
      
      public String getNodeNamespace() {
          return null;
      }
    }

   public Object getTestBean()
    {
        Map testBean = new TestBean();
        testBean.put("name", "Chris");
        testBean.put("location", "San Francisco");
        testBean.put("age", new Integer(27));
        return testBean;
    }

    public static class TestBean extends HashMap {
        /**
		 * 
		 */
		private static final long serialVersionUID = 2531504146861186823L;
		public String getName() {
            return "Christopher";
        }
        public int getLuckyNumber() {
            return 7;
        }
    }
}
