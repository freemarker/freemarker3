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

package freemarker.cache;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;

/**
 * A {@link TemplateLoader} that uses streams reachable through 
 * {@link Class#getResourceAsStream(String)} as its source of templates.
 * @author Attila Szegedi, szegedia at freemail dot hu
 * @version $Id: ClassTemplateLoader.java,v 1.11 2005/10/10 21:42:12 ddekany Exp $
 */
public class ClassTemplateLoader extends URLTemplateLoader
{
    private final Class loaderClass;
    private String path;
    private CodeSource classCodeSource; 
    
    /**
     * Creates a template loader that will use the {@link Class#getResource(String)}
     * method of its own class to load the resources, and <code>"/"</code> as base path.
     * This means that that template paths will be resolved relatvively the root package
     * of the class hierarchy, so you hardly ever should use this constructor, rather do
     * something like this:<br>
     * {@link #ClassTemplateLoader(Class, String)
     * new ClassTemplateLoader(com.example.myapplication.SomeClass.class, "templates")}
     *
     * <p>If you extend this class, then the extending class will be used to load
     * the resources.
     *
     * <p>Warning: this constructor was malfunctioned prior FreeMarker 2.3.4
     * -- please update FreeMarker if needed.
     *
     * @deprecated confusing constructor, and seldom useful;
     *     use {@link #ClassTemplateLoader(Class, String)} instead.
     */
    public ClassTemplateLoader()
    {
        this(null, "/", false);
    }

    /**
     * Creates a template loader that will use the {@link Class#getResource(String)}
     * method of the specified class to load the resources, and <code>""</code> as base
     * path. This means that template paths will be resolved relatively to the class
     * location, that is, relatively to the directory (package) of the class.
     *
     * @param loaderClass the class whose
     * {@link Class#getResource(String)} will be used to load the templates.
     *
     * @deprecated it is confusing that the base path is <code>""</code>;
     *     use {@link #ClassTemplateLoader(Class, String)} instead.
     */
    public ClassTemplateLoader(Class loaderClass)
    {
        this(loaderClass, "", false);
    }

    /**
     * Equivalent to invoking {@link #ClassTemplateLoader(Class, String, boolean)}
     * with <tt>false</tt> as the last argument.
     * @param loaderClass the class whose {@link Class#getResource(String)} 
     * method will be used to load the templates. Be sure that you chose a 
     * class whose defining class-loader sees the templates. This parameter 
     * can't be <code>null</code>.
     * @param path the base path to template resources. A path that doesn't 
     * start with a slash (/) is relative to the path (package) of the 
     * specified class. A path that starts with a slash is an absolute path 
     * starting from the root of the package hierarchy. Path components should 
     * be separated by forward slashes independently of the separator character
     * used by the underlying operating system. This parameter can't be 
     * <code>null</code>.
     */
    public ClassTemplateLoader(Class loaderClass, String path) {
        this(loaderClass, path, false);
    }
    
    /**
     * Creates a template loader that will use the 
     * {@link Class#getResource(String)} method of the specified class to load
     * the resources, and the specified base path (absolute or relative).
     *
     * <p>Examples:
     * <ul>
     *   <li>Relative base path (will load from the
     *       <code>com.example.myapplication.templates</code> package):<br>
     *       <code>new ClassTemplateLoader(<br>
     *       com.example.myapplication.SomeClass.class,<br>
     *       "templates")</code>
     *   <li>Absolute base path:<br>
     *       <code>new ClassTemplateLoader(<br>
     *       somepackage.SomeClass.class,<br>
     *       "/com/example/myapplication/templates")</code>
     * </ul>
     *
     * @param loaderClass the class whose {@link Class#getResource(String)} method will be used
     *     to load the templates. Be sure that you chose a class whose defining class-loader
     *     sees the templates. This parameter can't be <code>null</code>.
     * @param path the base path to template resources.
     *     A path that doesn't start with a slash (/) is relative to the
     *     path (package) of the specified class. A path that starts with a slash
     *     is an absolute path starting from the root of the package hierarchy. Path
     *     components should be separated by forward slashes independently of the
     *     separator character used by the underlying operating system.
     *     This parameter can't be <code>null</code>.
     * @param useClassCodeSource if true, the code source of the class is used
     * as the coude source for all templates loaded through this loader. If
     * false, then {@link URLTemplateLoader#getCodeSource(Object)} will be used
     * to obtain (a different) code source for each template.
     */
    public ClassTemplateLoader(Class loaderClass, String path, 
            boolean useClassCodeSource)
    {
        if(path == null)
        {
            throw new IllegalArgumentException("path == null");
        }
        this.loaderClass = loaderClass == null ? this.getClass() : loaderClass;
        this.path = canonicalizePrefix(path);
        if(useClassCodeSource) {
            classCodeSource = AccessController.doPrivileged(
                    new PrivilegedAction<CodeSource>()
                    {
                        public CodeSource run()
                        {
                            return ClassTemplateLoader.this.loaderClass.getProtectionDomain().getCodeSource();
                        }
                    });
        } else {
            classCodeSource = null;
        }
    }

    protected URL getURL(String name)
    {
        return loaderClass.getResource(path + name);
    }

    public CodeSource getCodeSource(Object templateSource) throws IOException
    {
        if(classCodeSource != null) {
            return classCodeSource;
        }
        return super.getCodeSource(templateSource);
    }
    
    
}