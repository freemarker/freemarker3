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
     * @param the configuration that is checked for being secured.
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
