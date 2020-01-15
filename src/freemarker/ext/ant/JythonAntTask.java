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

package freemarker.ext.ant;

import java.io.*;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;

import freemarker.template.utility.ClassUtil;

/**
 * Used internally, public for technical reasons only.
 * 
 * <p>This is an Ant sub-task of the {@link FreemarkerXmlTask} task.
 * 
 * <p><b>Warning! It must not be statically linked with Jython classes</b>,
 * so users can use the XML ant task even if Jython is not installed. 
 */
public class JythonAntTask extends Task {
    
    private File scriptFile;
    private String script = "";
    private UnlinkedJythonOperations jythonOps;
    
    public void setFile(File scriptFile) throws BuildException {
        ensureJythonOpsExists();
        this.scriptFile = scriptFile;
    }
    
    public void addText(String text) {
        script += text;
    }
    
    public void execute(Map vars) throws BuildException {
        if (scriptFile != null) {
            ensureJythonOpsExists();
            jythonOps.execute(scriptFile, vars);
        } 
        if (script.trim().length() >0) {
            ensureJythonOpsExists();
            String finalScript = ProjectHelper.replaceProperties(
                    project, script, project.getProperties());
            jythonOps.execute(finalScript, vars);
        }
    }

    private void ensureJythonOpsExists() {
        if (jythonOps == null) {
            Class clazz;
            try {
                clazz = ClassUtil.forName(
                        "freemarker.ext.ant.UnlinkedJythonOperationsImpl");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                        "A ClassNotFoundException has been thrown when trying "
                        + "to get the "
                        + "freemarker.ext.ant.UnlinkedJythonOperationsImpl class. "
                        + "The error message was: " + e.getMessage());
            }
            try {
                jythonOps
                        = (UnlinkedJythonOperations) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(
                        "An exception has been thrown when trying "
                        + "to create a freemarker.ext.ant.JythonAntTask "
                        + "object. The exception was: " + e);
            }
        }
    }
    
}
