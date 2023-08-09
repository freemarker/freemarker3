package freemarker.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * A {@link TemplateLoader} that uses files in a specified directory as the
 * source of templates. If contains security checks that will prevent it
 * serving templates outside the template directory (like <code>&lt;include /etc/passwd></code>.
 * It compares canonical paths for this, so templates that are symbolically
 * linked into the template directory from outside of it won't work either.
 * @author Attila Szegedi, szegedia at freemail dot hu
 * @version $Id: FileTemplateLoader.java,v 1.26 2004/03/29 08:06:22 szegedia Exp $
 */
public class FileTemplateLoader implements TemplateLoader {
    private static final boolean SEP_IS_SLASH = File.separatorChar == '/';
    /**
     * The base directory used as the root of the namespace for resolving
     * template paths.
     */
    public final File baseDir;
    private String canonicalPath;
    
    /**
     * Creates a new file template cache that will use the current directory
     * (the value of the system property <code>user.dir</code> as the base
     * directory for loading templates. The code source URL of all templates 
     * will be set to the directory, so a single set of permissions that 
     * applies to the directory will apply to all templates.
     * @throws IOException if an I/O exception occurs
     */
    public FileTemplateLoader() throws IOException {
        this(new File(System.getProperty("user.dir")));
    }

    /**
     * Creates a new file template loader that will use the specified directory
     * as the base directory for loading templates. The code source URL of all 
     * templates will be set to the directory, so a single set of permissions 
     * that applies to the directory will apply to all templates.
     * @param baseDir the base directory for loading templates
     * @throws IOException if an I/O exception occurs
     */
    public FileTemplateLoader(File baseDir) throws IOException {
        this(baseDir, false);
    }
    
    /**
     * Creates a new file template loader that will use the specified directory
     * as the base directory for loading templates.
     * @param baseDir the base directory for loading templates
     * @param allowLinking if true, it will allow following symlinks pointing
     * outside the baseDir
     * @throws IOException if an I/O exception occurs
     */
    public FileTemplateLoader(final File baseDir, final boolean allowLinking) throws IOException {
        this(baseDir, allowLinking, true);
    }
    
    /**
     * Creates a new file template loader that will use the specified directory
     * as the base directory for loading templates and a specified code source
     * policy.
     * @param baseDir the base directory for loading templates
     * @param allowLinking if true, it will allow following symlinks pointing
     * outside the baseDir
     * @param useBaseDirCodeSource if true, the code source URL of all 
     * templates will be set to the file: URL of the directory, so a single set
     * of permissions that applies to the directory will apply to all 
     * templates. If false, the code source URL of each template will be the
     * file: URL of that template, so a finer-grained access control can be 
     * specified in the policy file at expense of having multiple code sources.
     * @throws IOException if an I/O exception occurs
     */
    public FileTemplateLoader(File baseDir, final boolean allowLinking,
            boolean useBaseDirCodeSource) throws IOException
    {
        if (!baseDir.exists()) {
            throw new FileNotFoundException(baseDir + " does not exist.");
        }
        if (!baseDir.isDirectory()) {
            throw new IOException(baseDir + " is not a directory.");
        }
        if (!allowLinking) {
            baseDir = baseDir.getCanonicalFile();
            canonicalPath = baseDir.getPath();
            // Most canonical paths don't end with File.separator,
            // but some does. Like, "C:\" VS "C:\templates".
            if (!canonicalPath.endsWith(File.separator)) {
                canonicalPath += File.separatorChar;
            }
        }
        this.baseDir = baseDir;
    }

    public Object findTemplateSource(final String name) throws IOException {
        File source = new File(baseDir, SEP_IS_SLASH ? name : name.replace('/', File.separatorChar));
        if (!source.isFile()) {
            return null;
        }
        // Security check for inadvertently returning something
        // outside the template directory when linking is not
        // allowed.
        if (canonicalPath != null) {
            String normalized = source.getCanonicalPath();
            if (!normalized.startsWith(canonicalPath)) {
                throw new SecurityException(source.getAbsolutePath()
                        + " resolves to " + normalized + " which " +
                        " doesn't start with " + canonicalPath);
            }
        }
        return source;
    }
    
    public long getLastModified(final Object templateSource) {
        return ((File) templateSource).lastModified();
    }
    
    public Reader getReader(final Object templateSource, final String encoding)
    throws IOException
    {
        return new InputStreamReader(new FileInputStream((File) templateSource), encoding);
    }
    
    public void closeTemplateSource(Object templateSource) {
        // Do nothing.
    }
}