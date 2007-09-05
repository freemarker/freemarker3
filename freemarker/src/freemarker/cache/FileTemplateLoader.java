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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;

import freemarker.template.utility.SecurityUtilities;

/**
 * A {@link TemplateLoader} that uses files in a specified directory as the
 * source of templates. If contains security checks that will prevent it
 * serving templates outside the template directory (like <code>&lt;include /etc/passwd></code>.
 * It compares canonical paths for this, so templates that are symbolically
 * linked into the template directory from outside of it won't work either.
 * The code source URL of the templates can be either globally set to the base 
 * directory, or if more fine-grained access control is required, then each
 * template can have a code source URL of its file.
 * @author Attila Szegedi, szegedia at freemail dot hu
 * @version $Id: FileTemplateLoader.java,v 1.26 2004/03/29 08:06:22 szegedia Exp $
 */
public class FileTemplateLoader implements SecureTemplateLoader
{
    private static final boolean SEP_IS_SLASH = File.separatorChar == '/';
    /**
     * The base directory used as the root of the namespace for resolving
     * template paths.
     */
    public final File baseDir;
    private final String canonicalPath;
    private final CodeSource baseDirCodeSource;
    
    /**
     * Creates a new file template cache that will use the current directory
     * (the value of the system property <code>user.dir</code> as the base
     * directory for loading templates. The code source URL of all templates 
     * will be set to the directory, so a single set of permissions that 
     * applies to the directory will apply to all templates.
     * @throws IOException if an I/O exception occurs
     */
    public FileTemplateLoader()
    throws
    	IOException
    {
        this(new File(SecurityUtilities.getSystemProperty("user.dir")), true);
    }

    /**
     * Creates a new file template loader that will use the specified directory
     * as the base directory for loading templates. The code source URL of all 
     * templates will be set to the directory, so a single set of permissions 
     * that applies to the directory will apply to all templates.
     * @param baseDir the base directory for loading templates
     * @throws IOException if an I/O exception occurs
     */
    public FileTemplateLoader(File baseDir)
    throws
        IOException
    {
        this(baseDir, true);
    }
    
    /**
     * Creates a new file template loader that will use the specified directory
     * as the base directory for loading templates and a specified code source
     * policy.
     * @param baseDir the base directory for loading templates
     * @param useBaseDirCodeSource if true, the code source URL of all 
     * templates will be set to the file: URL of the directory, so a single set
     * of permissions that applies to the directory will apply to all 
     * templates. If false, the code source URL of each template will be the
     * file: URL of that template, so a finer-grained access control can be 
     * specified in the policy file at expense of having multiple code sources.
     * @throws IOException if an I/O exception occurs
     */
    public FileTemplateLoader(final File baseDir, boolean useBaseDirCodeSource)
    throws
    	IOException
    {
        try
        {
            Object[] retval = AccessController.doPrivileged(
                new PrivilegedExceptionAction<Object[]>()
                {
                    public Object[] run() throws IOException
                    {
                        if (!baseDir.exists())
                        {
                            throw new FileNotFoundException(baseDir + " does not exist.");
                        }
                        if (!baseDir.isDirectory())
                        {
                            throw new IOException(baseDir + " is not a directory.");
                        }
                        Object[] retval2 = new Object[3];
                        retval2[0] = baseDir.getCanonicalFile();
                        retval2[1] = ((File) retval2[0]).getPath() + File.separatorChar;
                        retval2[2] = baseDir.toURL();
                        return retval2;
                    }
                });
            this.baseDir = (File) retval[0];
            this.canonicalPath = (String) retval[1];
            if(useBaseDirCodeSource) {
                baseDirCodeSource = new CodeSource((URL)retval[2], 
                        (Certificate[])null);
            } else {
                baseDirCodeSource = null;
            }
        }
        catch(PrivilegedActionException e)
        {
            throw (IOException)e.getException();
        }
    }
    
    public Object findTemplateSource(final String name)
    throws
    	IOException
    {
        try
        {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<File>()
            {
                public File run()
                throws
                    IOException
                {
                    File source = new File(baseDir, SEP_IS_SLASH ? name : name.replace('/', File.separatorChar));
                    if(!source.isFile()) {
                        return null;
                    }
                    // Security check for inadvertently returning something outside the
                    // template directory.
                    String normalized = source.getCanonicalPath(); 
                    if (normalized.startsWith(canonicalPath)) {
                        return source;
                    }
                    throw new SecurityException(normalized + " doesn't start with " + canonicalPath);
                }
            });
        }
        catch(PrivilegedActionException e)
        {
            throw (IOException)e.getException();
        }
    }
    
    public long getLastModified(final Object templateSource)
    {
        return AccessController.doPrivileged(new PrivilegedAction<Long>() {
            public Long run() {
            	return Long.valueOf(((File) templateSource).lastModified());
            }
        }).longValue();
    }
    
    public Reader getReader(final Object templateSource, final String encoding)
    throws
        IOException
    {
        // TODO: this shouldn't really be in a doPrivileged() as unprivileged
        // code can use it to read the file by loading a template from it and
        // then rendering it into its own writer. The question is whether we 
        // can modify it without breaking BWC... This is actually a security
        // bugfix.
        try
        {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Reader>() {
                public Reader run() throws IOException
                {
                    if (!(templateSource instanceof File)) {
                        throw new IllegalArgumentException(
                                "templateSource is a: " + templateSource.getClass().getName());
                    }
                    return new InputStreamReader(new FileInputStream((File) templateSource), encoding);
                }
            });
        }
        catch(PrivilegedActionException e)
        {
            throw (IOException)e.getException();
        }
    }
    
    public void closeTemplateSource(Object templateSource)
    {
        // Do nothing.
    }
    
    public CodeSource getCodeSource(Object templateSource) throws IOException
    {
        if(baseDirCodeSource != null)
        {
            return baseDirCodeSource;
        }
        File f = (File)templateSource;
        return new CodeSource(f.toURL(), (Certificate[])null);
    }

}