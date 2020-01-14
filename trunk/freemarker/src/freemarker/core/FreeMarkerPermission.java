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

import java.security.BasicPermission;
import java.security.Permission;

import freemarker.ext.script.FreeMarkerScriptConstants;
import freemarker.ext.script.FreeMarkerScriptEngine;
import freemarker.ext.script.FreeMarkerScriptEngineFactory;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * A class representing all FreeMarker-related permissions. Currently available
 * permissions are:
 * <table>
 *   <tr>
 *     <td>modifyTemplate</td>
 *     <td>Permits invocation of various template methods that can be used to
 *     modify the template in such a way as to breach security. Specifically, 
 *     {@link Template#getRootTreeNode()}, {@link Template#getMacros()}, 
 *     {@link Template#setParent(Configurable)}, and 
 *     {@link Template#containingElements(int, int)}.
 *   </tr>
 *   <tr>
 *     <td>setSecure</td>
 *     <td>Permits invocation of {@link Configuration#setSecure(boolean)}
 *     with false argument (thus turning off security of a secure configuration).</td>
 *   </tr>
 *   <tr>
 *     <td>setScriptEngineConfiguration</td>
 *     <td>Permits invocation of 
 *     {@link FreeMarkerScriptEngine#setConfiguration(
 *     freemarker.template.Configuration)} or specifying a configuration object
 *     within the engine's bindings under name 
 *     {@link FreeMarkerScriptConstants#CONFIGURATION}.</td>
 *   </tr>
 *   <tr>
 *     <td>setScriptEngineFactoryConfiguration</td>
 *     <td>Permits invocation of 
 *     {@link FreeMarkerScriptEngineFactory#setConfiguration(
 *     freemarker.template.Configuration)}.</td>
 *   </tr>
 *   <tr>
 *     <td>setTemplateLoader</td>
 *     <td>Permits setting an arbitrary template loader using 
 *     {@link Configuration#setTemplateLoader(freemarker.cache.TemplateLoader)}.
 *     Note that various <tt>setXxxForTemplateLoading()</tt> methods are not
 *     checked for this permission, as they internally all create trusted 
 *     template loaders.</td>
 *   </tr>
 * </table>
 * @author Attila Szegedi
 * @version $Id: $
 */
public class FreeMarkerPermission extends BasicPermission
{
    private static final long serialVersionUID = 1L;

    public FreeMarkerPermission(String name)
    {
        super(name);
    }
    
    /**
     * Checks a permission if there is a SecurityManager installed in the JVM
     * and the Configuration's isSecure() returns true.
     * @param config the configuration that is checked for being secured.
     * @param permission the permission to check
     * @throws SecurityException if the permission check fails.
     */
    public static void checkPermission(Configuration config, Permission permission) {
        if(config.isSecure()) {
            checkPermission(permission);
        }
    }
    
    /**
     * Checks a permission if there is a SecurityManager installed in the JVM.
     * @param permission the permission to check
     * @throws SecurityException if the permission check fails.
     */
    public static void checkPermission(Permission permission) {
        SecurityManager sm = System.getSecurityManager();
        if(sm != null) {
            sm.checkPermission(permission);
        }
    }
}
