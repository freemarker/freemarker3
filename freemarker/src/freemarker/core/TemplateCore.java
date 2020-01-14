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

package freemarker.core;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

import freemarker.core.ast.Macro;
import freemarker.core.ast.TemplateElement;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Abstract base class for {@link Template}. Used to provide package-level 
 * access to secured internals used by {@link Environment}.
 * @author Attila Szegedi
 * @version $Id: $
 */
public abstract class TemplateCore extends Configurable
{
    private static final Permission MODIFY_TEMPLATE = new FreeMarkerPermission("modifyTemplate");

    private TemplateElement rootElement;
    private Map<String, Macro> macros = new HashMap<String, Macro>();
    
    protected TemplateCore(Configuration config) {
        super(config);
    }
    
    /**
     * Called by code internally to maintain
     * a table of macros
     *  @throws SecurityException if the macro with this name already exists,
     *  getConfiguration().isSecure() returns true, there is a security manager
     *  in the JVM, and the caller of this method does not posess the 
     *  "modifyTemplate" FreeMarker permission.
     */
    public void addMacro(Macro macro) {
        String macroName = macro.getName();
        synchronized(macros) {
            if(macros.containsKey(macroName)) {
                checkModifyTemplate();
            }
            macros.put(macroName, macro);
        }
    }

    protected static void checkModifyTemplate() {
        FreeMarkerPermission.checkPermission(MODIFY_TEMPLATE);
    }
    
    /**
     *  @throws SecurityException if the getConfiguration().isSecure()
     *  returns true, there is a security manager in the JVM, and the caller
     *  of this method does not posess the "modifyTemplate" FreeMarker 
     *  permission (since both the retrieved map and the macros in it are 
     *  mutable).
     */
    public Map<String,Macro> getMacros() {
        checkModifyTemplate();
        return getMacrosNoCheck();
    }
    
    Map<String,Macro> getMacrosNoCheck() {
        return macros;
    }
    
    /**
     * Dump the raw template in canonical form.
     */
    public void dump(PrintStream ps) {
        ps.print(rootElement.getCanonicalForm());
    }

    /**
     * Dump the raw template in canonical form.
     */
    public void dump(Writer out) throws IOException {
        out.write(rootElement.getCanonicalForm());
    }
    
    protected synchronized TemplateElement getRootElement() {
        return rootElement;
    }
    
    protected synchronized void setRootElement(TemplateElement rootElement) {
        if(this.rootElement != null) {
            throw new IllegalStateException("Root element already set");
        }
        this.rootElement = rootElement;
    }
}
