package freemarker.docgen;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Ant task to invoke Transform.
 * $Id: DocgenTransformTask.java,v 1.1 2004/03/13 18:37:34 ddekany Exp $
 */

public class DocgenTransformTask extends Task
{
    // the task's parameters
    private Properties myProperties = new Properties();
    
    // setters for ant
    public void setOutputDir(File value) {
        myProperties.setProperty(Transform.PROP_OUTPUT_DIR, value.getAbsolutePath());
    }
    
    public void setTemplateDir(File value) {
        myProperties.setProperty(Transform.PROP_TEMPLATE_DIR, value.getAbsolutePath());
    }
    
    public void setSrcFile(File value) {
        myProperties.setProperty(Transform.PROP_SRC_FILE, value.getAbsolutePath());
    }
    
    public void setShowEditorNotes(boolean value) {
        myProperties.setProperty(Transform.PROP_SHOW_EDITOR_NOTES, String.valueOf(value));
    }
    
    public void setOutputWarnings(boolean value) {
        myProperties.setProperty(Transform.PROP_OUTPUT_WARNINGS, String.valueOf(value));
    }
    
    public void setOlinksFile(File file) throws BuildException {
     	Properties olinks = new Properties();
     	try {
     		FileInputStream in = new FileInputStream(file);
     		olinks.load(in);
     		in.close();
     		for (Map.Entry<Object, Object> entry : olinks.entrySet()) {
     			myProperties.put("link." + entry.getKey(), entry.getValue());
     		}
     	} catch (Exception e) {
     		throw new BuildException(e);
     	}
    }
    
    public void setLocale(String locString) {
    	myProperties.setProperty("locale", locString);
    	
    }
    
    public void setTimeZone(String timeZone) {
    	myProperties.setProperty("timeZone", timeZone);
    }

    // just start the transformation
    public void execute() throws BuildException {
        try {
            Transform.startTransformation(myProperties);
        }
        catch (Exception e) {
            throw new BuildException(e);
        }
    }
}