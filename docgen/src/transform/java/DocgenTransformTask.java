/**
 * Ant task to invoke Transform.
 * $Id: DocgenTransformTask.java,v 1.1 2004/03/13 18:37:34 ddekany Exp $
 */

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import java.io.File;
import java.util.Properties;

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
    
    public void setStopOnValidationError(boolean value) {
        myProperties.setProperty(Transform.PROP_STOP_ON_VALIDATION_ERROR, String.valueOf(value));
    }
    
    public void setOutputWarnings(boolean value) {
        myProperties.setProperty(Transform.PROP_OUTPUT_WARNINGS, String.valueOf(value));
    }

    /*
    public void setCatalogFiles(String value) {
        myProperties.setProperty(Transform.PROP_CATALOG_FILES, value);
    }
    */
    
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