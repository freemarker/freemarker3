package freemarker3.testcase;

import junit.framework.*;
//import freemarker3.ext.dom.NodeModel;
import javax.xml.parsers.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import org.w3c.dom.*;


/**
 * Test suite for FreeMarker. The suite conforms to interface expected by
 * <a href="http://junit.sourceforge.net/" target="_top">JUnit</a>.
 *
 * @version $Id: TemplateTestSuite.java,v 1.8 2005/06/11 15:18:26 revusky Exp $
 */
public class TemplateTestSuite extends TestSuite {
    
    private Map configParams = new LinkedHashMap();
    
    public static TestSuite suite() throws Exception {
        return new TemplateTestSuite();
    }
    
    public TemplateTestSuite() throws Exception {
        readConfig();
    }
    
    void readConfig() throws Exception {
//        java.net.URL url = TemplateTestSuite.class.getResource("testcases.xml");
//        File f = new File(url.getFile());
        File f= new File("src/freemarker3/testcase/testcases.xml");
        readConfig(f);
    }
    
    /**
     * Read the testcase configurations file and build up the test suite
     */
    public void readConfig(File f) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //dbf.setValidating(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document d = db.parse(f);
        Element root = d.getDocumentElement();
        NodeList children = root.getChildNodes();
        File baseDir = f.getParentFile();
        for (int i=0; i<children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equals("config")) {
                    NamedNodeMap atts = n.getAttributes();
                    for (int j=0; j<atts.getLength(); j++) {
                        Attr att = (Attr) atts.item(j);
                        configParams.put(att.getName(), att.getValue());
                    }
                }
                if (n.getNodeName().equals("testcase")) {
                    TestCase tc = createTestCaseFromNode((Element) n, baseDir);
                    addTest(tc);
                }
            }
        }
    }
    
    String getTextInElement(Element e) {
        StringBuilder buf = new StringBuilder();
        NodeList children = e.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            Node n = children.item(i);
            short type = n.getNodeType();
            if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
                buf.append(n.getNodeValue());
            }
        }
        return buf.toString();
    }
    
    /**
     * Takes as in put the dom node that specifies the testcase
     * and instantiates a testcase. If class is not specified,
     * it uses the TemplateTestCase class. If the class is specified,
     * it must be a TestCase class and have a constructor that 
     * takes two strings as parameters.
     */
    TestCase createTestCaseFromNode(Element e, File baseDir) throws Exception {
        String filename = e.getAttribute("filename");
        String name = e.getAttribute("name");
        String classname = e.getAttribute("class");
        if (classname != null && classname.length() >0) {
            Class cl = Class.forName(classname);
            Constructor cons = cl.getConstructor(new Class[] {String.class, String.class});
            return (TestCase) cons.newInstance(new Object [] {name, filename});
        } 
        TemplateTestCase result = new TemplateTestCase(name, filename);
        for (Iterator it=configParams.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            System.out.println("Setting " + key +  " to " + value);
            result.setConfigParam(entry.getKey().toString(), entry.getValue().toString(), baseDir);
        }
        NodeList configs = e.getElementsByTagName("config");
        for (int i=0; i<configs.getLength(); i++)  {
            NamedNodeMap atts = configs.item(i).getAttributes();
            for (int j=0; j<atts.getLength(); j++) {
                Attr att = (Attr) atts.item(j);
                result.setConfigParam(att.getName(), att.getValue(), baseDir);
            }
        }
        return result;
    }
    
    
    

    public static void main (String[] args) throws Exception {
        
        junit.textui.TestRunner.run(new TemplateTestSuite());
//       junit.swingui.TestRunner.run (TemplateTestSuite.class);
//        junit.awtui.TestRunner.run (TemplateTestSuite.class);
    }
}
