package freemarker.template;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import freemarker.cache.*;
import freemarker.core.Configurable;
import freemarker.core.Environment;
import freemarker.core.variables.Hash;
import freemarker.core.variables.WrappedVariable;
import freemarker.core.parser.ParseException;
import freemarker.core.parser.ParsingProblem;
import freemarker.log.Logger;
import freemarker.template.utility.HtmlEscape;
import freemarker.template.utility.StandardCompress;
import freemarker.template.utility.StringUtil;
import freemarker.template.utility.XmlEscape;

import static freemarker.core.variables.Wrap.*;

/**
 * Main entry point into the FreeMarker API, this class encapsulates the 
 * various configuration parameters with which FreeMarker is run, as well
 * as serves as a central template loading and caching point. Note that
 * this class uses a default strategy for loading 
 * and caching templates. You can plug in a replacement
 * template loading mechanism by using the {@link #setTemplateLoader(TemplateLoader)}
 * method.
 *
 * This object is <em>not synchronized</em>. Thus, the settings must not be changed
 * after you have started to access the object from multiple threads. If you use multiple
 * threads, set everything directly after you have instantiated the <code>Configuration</code>
 * object, and don't change the settings anymore.
 *
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 * @author Attila Szegedi
 */

@SuppressWarnings("deprecation")
public class Configuration extends Configurable {

    private static final Logger logger = Logger.getLogger("freemarker.parser");
    private static Configuration defaultConfig = new Configuration();
    private boolean localizedLookup = true, legacySyntax;
    private TemplateCache cache;
    private HashMap<String, Object> variables = new HashMap<String, Object>();
    private HashMap<String, String> encodingMap = new HashMap<String, String>();
    private Map<String, String> autoImportMap = new HashMap<String, String>();
    private ArrayList<String> autoImports = new ArrayList<String>();
    private ArrayList<String> autoIncludes = new ArrayList<String>();
    private String defaultEncoding = "UTF-8";
    private boolean tolerateParsingProblems = false;

    public Configuration() {
        cache = new TemplateCache();
        cache.setConfiguration(this);
        cache.setDelay(5000);
        loadBuiltInSharedVariables();
    }
    
    public void setTemplateCache(TemplateCache cache) {
    	this.cache = cache;
    	cache.setConfiguration(this);
    	cache.setDelay(5000);
    }
    
    public TemplateCache getTemplateCache() {
    	return cache;
    }
    
    private void loadBuiltInSharedVariables() {
        variables.put("compress", StandardCompress.INSTANCE);
        variables.put("html_escape", new HtmlEscape());
        variables.put("xml_escape", new XmlEscape());
    }

    /**
     * Returns the default (singleton) Configuration object. Note that you can
     * create as many separate configurations as you wish; this global instance
     * is provided for convenience, or when you have no reason to use a separate
     * instance.
     * 
     * @deprecated The usage of the static singleton (the "default")
     * {@link Configuration} instance can easily cause erroneous, unpredictable
     * behavior. This is because multiple independent software components may use
     * FreeMarker internally inside the same application, so they will interfere
     * because of the common {@link Configuration} instance. Each such component
     * should use its own private {@link Configuration} object instead, that it
     * typically creates with <code>new Configuration()</code> when the component
     * is initialized.
     */
    static public Configuration getDefaultConfiguration() {
        return defaultConfig;
    }
    
    /**
     * 
     * @return the {@link Configuration} object that is being used
     * in this template processing thread.
     */
    
    static public Configuration getCurrentConfiguration() {
    	Environment env = Environment.getCurrentEnvironment();
    	return env != null ? env.getConfiguration() : defaultConfig;
    }
    
  
    /**
     * Sets the Configuration object that will be retrieved from future calls
     * to {@link #getDefaultConfiguration()}.
     * 
     * @deprecated Using the "default" {@link Configuration} instance can
     * easily lead to erroneous, unpredictable behaviour.
     * See more {@link Configuration#getDefaultConfiguration() here...}.
     */
    static public void setDefaultConfiguration(Configuration config) {
        defaultConfig = config;
    }
    
    /**
     * Sets a template loader that is used to look up and load templates.
     * By providing your own template loader, you can customize the way
     * templates are loaded. Several convenience methods in this class already
     * allow you to install commonly used loaders:
     * {@link #setClassForTemplateLoading(Class, String)}, 
     * {@link #setDirectoryForTemplateLoading(File)}, and
     * {@link #setServletContextForTemplateLoading(Object, String)}. By default,
     * a multi-loader is used that first tries to load a template from the file
     * in the current directory, then from a resource on the classpath.
     */
    public synchronized void setTemplateLoader(TemplateLoader loader) {
        createTemplateCache(loader, cache.getCacheStorage());
    }
    
    private void setTemplateLoaderNoCheck(TemplateLoader loader) {
        createTemplateCache(loader, cache.getCacheStorage());
    }

    private void createTemplateCache(TemplateLoader loader, CacheStorage storage)
    {
        TemplateCache oldCache = cache;
        cache = new TemplateCache(loader, storage);
        cache.setDelay(oldCache.getDelay());
        cache.setConfiguration(this);
        cache.setLocalizedLookup(localizedLookup);
    }
    /**
     * @return the template loader that is used to look up and load templates.
     * @see #setTemplateLoader
     */
    public synchronized TemplateLoader getTemplateLoader()
    {
        return cache.getTemplateLoader();
    }

    public synchronized void setCacheStorage(CacheStorage storage) {
        createTemplateCache(cache.getTemplateLoader(), storage);
    }
    
    /**
     * Set the explicit directory from which to load templates.
     */
    public void setDirectoryForTemplateLoading(File dir) throws IOException {
        TemplateLoader tl = getTemplateLoader();
        if (tl instanceof FileTemplateLoader) {
            String path = ((FileTemplateLoader) tl).baseDir.getCanonicalPath();
            if (path.equals(dir.getCanonicalPath()))
                return;
        }
        setTemplateLoaderNoCheck(new FileTemplateLoader(dir));
    }

    /**
     * Sets the servlet context from which to load templates
     * @param sctxt the ServletContext object. Note that the type is <code>Object</code>
     *        to prevent class loading errors when user who uses FreeMarker not for
     *        servlets has no javax.servlet in the CLASSPATH.
     * @param path the path relative to the ServletContext.
     * If this path is absolute, it is taken to be relative
     * to the server's URL, i.e. http://myserver.com/
     * and if the path is relative, it is taken to be 
     * relative to the web app context, i.e.
     * http://myserver.context.com/mywebappcontext/
     */
    public void setServletContextForTemplateLoading(Object sctxt, String path) {
        try {
            if (path == null) {
                setTemplateLoaderNoCheck( (TemplateLoader)
                        Class.forName("freemarker.cache.WebappTemplateLoader")
                            .getConstructor(new Class[]{Class.forName("javax.servlet.ServletContext")})
                                    .newInstance(new Object[]{sctxt}) );
            }
            else {
                setTemplateLoaderNoCheck( (TemplateLoader)
                        Class.forName("freemarker.cache.WebappTemplateLoader")
                            .getConstructor(new Class[]{Class.forName("javax.servlet.ServletContext"), String.class})
                                    .newInstance(new Object[]{sctxt, path}) );
            }
        } catch (Exception exc) {
            throw new RuntimeException("Internal FreeMarker error: " + exc);
        }
    }

    /**
     * Sets a class relative to which we do the 
     * Class.getResource() call to load templates.
     */
    public void setClassForTemplateLoading(Class clazz, String pathPrefix) {
        setTemplateLoaderNoCheck(new ClassTemplateLoader(clazz, pathPrefix));
    }

    /**
     * Set the time in seconds that must elapse before checking whether there is a newer
     * version of a template file.
     * This method is thread-safe and can be called while the engine works.
     */
    public void setTemplateUpdateDelay(int delay) {
        cache.setDelay(1000L * delay);
    }
    
    public void setStrictVariableDefinition(boolean b) {
    	this.legacySyntax = !b;
    }

    /**
     * Sets whether, by default, templates accep legacy syntax. 
     * At the moment, the factory-set default is off.
     */
    
    public void setLegacySyntax(boolean b) {
        this.legacySyntax = b;
    }
    
    /**
     * @return whether, by default, templates use strict variable
     * definition syntax, such that any variable created 
     * at the top template level must be declared with a #var
     * directive. At the moment, the factory-set default is on.
     */
    
    public boolean getStrictVariableDefinition() {
    	return !legacySyntax;
    }

    /**
     * Equivalent to <tt>getTemplate(name, thisCfg.getLocale(), thisCfg.getEncoding(thisCfg.getLocale()), true)</tt>.
     */
    public Template getTemplate(String name) throws IOException {
        Locale loc = getLocale();
        return getTemplate(name, loc, getEncoding(loc), true);
    }

    /**
     * Equivalent to <tt>getTemplate(name, locale, thisCfg.getEncoding(locale), true)</tt>.
     */
    public Template getTemplate(String name, Locale locale) throws IOException {
        return getTemplate(name, locale, getEncoding(locale), true);
    }

    /**
     * Equivalent to <tt>getTemplate(name, thisCfg.getLocale(), encoding, true)</tt>.
     */
    public Template getTemplate(String name, String encoding) throws IOException {
        return getTemplate(name, getLocale(), encoding, true);
    }

    /**
     * Equivalent to <tt>getTemplate(name, locale, encoding, true)</tt>.
     */
    public Template getTemplate(String name, Locale locale, String encoding) throws IOException {
        return getTemplate(name, locale, encoding, true);
    }

    /**
     * Retrieves a template specified by a name and locale, interpreted using
     * the specified character encoding, either parsed or unparsed. For the
     * exact semantics of parameters, see 
     * {@link TemplateCache#getTemplate(String, Locale, String, boolean)}.
     * @return the requested template.
     * @throws FileNotFoundException if the template could not be found.
     * @throws IOException if there was a problem loading the template.
     * @throws ParseException (extends <code>IOException</code>) if the template is syntactically bad.
     */
    public Template getTemplate(String name, Locale locale, String encoding, boolean parse) throws IOException {
        Template result = cache.getTemplate(name, locale, encoding, parse);
        if (result == null) {
            throw new FileNotFoundException("Template " + name + " not found.");
        }
        if (result.hasParsingProblems() && !tolerateParsingProblems) {
            for (ParsingProblem pp : result.getParsingProblems()) {
                logger.error(pp.getMessage());
                //System.err.println(pp.getMessage());
            }
            throw new ParseException(result.getParsingProblems());
        } else {
            for (ParsingProblem pp : result.getParsingProblems()) {
                logger.warn(pp.getMessage());
            }
        }
        return result;
    }

    /**
     * Sets the default encoding for converting bytes to characters when
     * reading template files in a locale for which no explicit encoding
     * was specified. Defaults to default system encoding.
     */
    public void setDefaultEncoding(String encoding) {
        defaultEncoding = encoding;
    }

    /**
     * Gets the default encoding for converting bytes to characters when
     * reading template files in a locale for which no explicit encoding
     * was specified. Defaults to default system encoding.
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Gets the preferred character encoding for the given locale, or the 
     * default encoding if no encoding is set explicitly for the specified
     * locale. You can associate encodings with locales using 
     * {@link #setEncoding(Locale, String)} or {@link #loadBuiltInEncodingMap()}.
     * @param loc the locale
     * @return the preferred character encoding for the locale.
     */
    public String getEncoding(Locale loc) {
        // Try for a full name match (may include country and variant)
        String charset = encodingMap.get(loc.toString());
        if (charset == null) {
            if (loc.getVariant().length() > 0) {
                Locale l = new Locale(loc.getLanguage(), loc.getCountry());
                charset = encodingMap.get(l.toString());
                if (charset != null) {
                    encodingMap.put(loc.toString(), charset);
                }
            } 
            charset = encodingMap.get(loc.getLanguage());
            if (charset != null) {
                encodingMap.put(loc.toString(), charset);
            }
        }
        return charset != null ? charset : defaultEncoding;
    }

    /**
     * Sets the character set encoding to use for templates of
     * a given locale. If there is no explicit encoding set for some
     * locale, then the default encoding will be used, what you can
     * set with {@link #setDefaultEncoding}.
     *
     * @see #clearEncodingMap
     * @see #loadBuiltInEncodingMap
     */
    public void setEncoding(Locale locale, String encoding) {
        encodingMap.put(locale.toString(), encoding);
    }

    /**
     * Adds a shared variable to the configuration.
     * Shared variables are variables that are visible
     * as top-level variables for all templates which use this
     * configuration, if the data model does not contain a
     * variable with the same name.
     *
     * <p>Never use <tt>WrappedVariable</tt> implementation that is not thread-safe for shared variables,
     * if the configuration is used by multiple threads! It is the typical situation for Servlet based Web sites.
     *
     * @param name the name used to access the data object from your template.
     *     If a shared variable with this name already exists, it will replace
     *     that.
     * @see #setSharedVariable(String,Object)
     * @see #setAllSharedVariables
     */
    public void setSharedVariable(String name, Object tm) {
        variables.put(name, wrap(tm));
    }

    public void put(String key, Object obj) {
        variables.put(key, wrap(obj));
    }

    /**
     * Returns the set containing the names of all defined shared variables.
     * The method returns a new Set object on each call that is completely
     * disconnected from the Configuration. That is, modifying the set will have
     * no effect on the Configuration object.
     */
    public Set<String> getSharedVariableNames() {
        return new HashSet<String>(variables.keySet());
    }
    
    /**
     * Gets a shared variable. Shared variables are variables that are 
     * available to all templates. When a template is processed, and an identifier
     * is undefined in the data model, a shared variable object with the same identifier
     * is then looked up in the configuration. There are several predefined variables
     * that are always available through this method, see the FreeMarker manual
     * for a comprehensive list of them.
     *
     * @see #setSharedVariable(String,Object)
     * @see #setSharedVariable(String,WrappedVariable)
     * @see #setAllSharedVariables
     */
    public Object getSharedVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Removes all shared variables, except the predefined ones (compress, html_escape, etc.).
     */
    public void clearSharedVariables() {
        variables.clear();
        loadBuiltInSharedVariables();
    }
    
    /**
     * Removes all entries from the template cache, thus forcing reloading of templates
     * on subsequent <code>getTemplate</code> calls.
     * This method is thread-safe and can be called while the engine works.
     */
    public void clearTemplateCache() {
        cache.clear();
    }
    
    /**
     * Returns if localized template lookup is enabled or not.
     * This method is thread-safe and can be called while the engine works.
     */
    public boolean getLocalizedLookup() {
        return cache.getLocalizedLookup();
    }
    
    /**
     * Enables/disables localized template lookup. Enabled by default.
     * This method is thread-safe and can be called while the engine works.
     */
    public void setLocalizedLookup(boolean localizedLookup) {
        this.localizedLookup = localizedLookup;
        cache.setLocalizedLookup(localizedLookup);
    }
    
    /**
     * Sets a setting by name and string value.
     *
     * In additional to the settings understood by
     * {@link Configurable#setSetting the super method}, it understands these:
     * <ul>
     *   <li><code>"auto_import"</code>: Sets the list of auto-imports. Example of valid value:
     *       <br><code>/lib/form.ftl as f, /lib/widget as w, "/lib/evil name.ftl" as odd</code>
     *       See: {@link #setAutoImports}
     *   <li><code>"auto_include"</code>: Sets the list of auto-includes. Example of valid value:
     *       <br><code>/include/common.ftl, "/include/evil name.ftl"</code>
     *       See: {@link #setAutoIncludes}
     *   <li><code>"default_encoding"</code>: The name of the charset, such as <code>"UTF-8"</code>.
     *       See: {@link #setDefaultEncoding}
     *   <li><code>"localized_lookup"</code>:
     *       <code>"true"</code>, <code>"false"</code>, <code>"yes"</code>, <code>"no"</code>,
     *       <code>"t"</code>, <code>"f"</code>, <code>"y"</code>, <code>"n"</code>.
     *       Case insensitive.
     *      See: {@link #setLocalizedLookup}
     *   <li><code>"strict_vars"</code>: <code>"true"</code>, <code>"false"</code>, etc.
     *       See: {@link #setStrictVariableDefinition}
     *   <li><code>"cache_storage"</code>: If the value contains dot, then it is
     *       interpreted as class name, and the object will be created with
     *       its parameterless constructor. If the value does not contain dot,
     *       then a {@link freemarker.cache.MruCacheStorage} will be used with the
     *       maximum strong and soft sizes specified with the setting value. Examples
     *       of valid setting values:
     *       <table border=1 cellpadding=4>
     *         <tr><th>Setting value<th>max. strong size<th>max. soft size
     *         <tr><td><code>"strong:50, soft:500"</code><td>50<td>500
     *         <tr><td><code>"strong:100, soft"</code><td>100<td><code>Integer.MAX_VALUE</code>
     *         <tr><td><code>"strong:100"</code><td>100<td>0
     *         <tr><td><code>"soft:100"</code><td>0<td>100
     *         <tr><td><code>"strong"</code><td><code>Integer.MAX_VALUE</code><td>0
     *         <tr><td><code>"soft"</code><td>0<td><code>Integer.MAX_VALUE</code>
     *       </table>
     *       The value is not case sensitive. The order of <tt>soft</tt> and <tt>strong</tt>
     *       entries is not significant.
     *       See also: {@link #setCacheStorage}
     *   <li><code>"template_update_delay"</code>: Valid positive integer, the
     *       update delay measured in seconds.
     *       See: {@link #setTemplateUpdateDelay}
     * </ul>
     *
     * @param key the name of the setting.
     * @param value the string that describes the new value of the setting.
     *
     * @throws UnknownSettingException if the key is wrong.
     * @throws TemplateException if the new value of the setting can't be set
     *     for any other reasons.
     */
    public void setSetting(String key, String value) {
        if ("TemplateUpdateInterval".equalsIgnoreCase(key)) {
            key = "template_update_delay";
        } else if ("DefaultEncoding".equalsIgnoreCase(key)) {
            key = "default_encoding";
        }
        try {
            if ("default_encoding".equalsIgnoreCase(key)) {
                setDefaultEncoding(value);
            } else if ("localized_lookup".equalsIgnoreCase(key)) {
                setLocalizedLookup(StringUtil.getYesNo(value));
            } else if ("strict_vars".equalsIgnoreCase(key)) {
            	setStrictVariableDefinition(StringUtil.getYesNo(value));
            } else if ("legacy_syntax".equalsIgnoreCase(key)) {
                setStrictVariableDefinition(!StringUtil.getYesNo(value));
            }
            else if ("cache_storage".equalsIgnoreCase(key)) {
                if (value.indexOf('.') == -1) {
                    int strongSize = 0;
                    int softSize = 0;
                    Map<String,String> map = StringUtil.parseNameValuePairList(
                            value, String.valueOf(Integer.MAX_VALUE));
                    for (Map.Entry<String, String> ent : map.entrySet()) {
                        String pname = ent.getKey();
                        int pvalue;
                        try {
                            pvalue = Integer.parseInt(ent.getValue());
                        } catch (NumberFormatException e) {
                            throw invalidSettingValueException(key, value);
                        }
                        if ("soft".equalsIgnoreCase(pname)) {
                            softSize = pvalue;
                        } else if ("strong".equalsIgnoreCase(pname)) {
                            strongSize = pvalue;
                        } else {
                            throw invalidSettingValueException(key, value);
                        }
                    }
                    if (softSize == 0 && strongSize == 0) {
                        throw invalidSettingValueException(key, value);
                    }
                    setCacheStorage(new MruCacheStorage(strongSize, softSize));
                } else {
                    setCacheStorage((CacheStorage) Class.forName(value)
                            .newInstance());
                }
            } else if ("template_update_delay".equalsIgnoreCase(key)) {
                setTemplateUpdateDelay(Integer.parseInt(value));
            } else if ("auto_include".equalsIgnoreCase(key)) {
                setAutoIncludes(new SettingStringParser(value).parseAsList());
            } else if ("auto_import".equalsIgnoreCase(key)) {
                setAutoImports(new SettingStringParser(value).parseAsImportList());
            } else {
                super.setSetting(key, value);
            }
        } catch(TemplateException e) {
            throw e;
        } catch(Exception e) {
            throw new TemplateException(
                    "Failed to set setting " + key + " to value " + value,
                    e, getEnvironment());
        }
    }
    
    
    /**
     * Add an auto-imported template.
     * The importing will happen at the top of any template that
     * is vended by this Configuration object.
     * @param namespace the name of the namespace into which the template is imported
     * @param template the name of the template
     */
    public synchronized void addAutoImport(String namespace, String template) {
        autoImports.remove(namespace);
        autoImports.add(namespace);
        autoImportMap.put(namespace, template);
    }
    
    /**
     * Remove an auto-imported template
     * @param namespace the name of the namespace into which the template was imported
     */
    
    public synchronized void removeAutoImport(String namespace) {
        autoImports.remove(namespace);
        autoImportMap.remove(namespace);
    }
    
    /**
     * set a map of namespace names to templates for auto-importing 
     * a set of templates. Note that all previous auto-imports are removed.
     */
    
    public synchronized void setAutoImports(Map<String, String> map) {
        autoImports = new ArrayList<String>(map.keySet());
       	autoImportMap = new HashMap<String, String>(map);
    }
    
    @Override
    protected void doAutoImportsAndIncludes(Environment env) throws IOException {
    	for (String namespace : autoImports) {
            String templateName = autoImportMap.get(namespace);
            env.importLib(templateName, namespace);
        }
    	for(String templateName: autoIncludes) {
            env.include(getTemplate(templateName, env.getLocale()), false);
        }
    }
    
    /**
     * add a template to be automatically included at the top of any template that
     * is vended by this Configuration object.
     * @param templateName the lookup name of the template.
     */
     
    public synchronized void addAutoInclude(String templateName) {
        autoIncludes.remove(templateName);
        autoIncludes.add(templateName);
    }

    /**
     * set the list of automatically included templates.
     * Note that all previous auto-includes are removed.
     */
    public synchronized void setAutoIncludes(List<String> templateNames) {
        autoIncludes.clear();
        autoIncludes.addAll(templateNames);
    }
    
    /**
     * remove a template from the auto-include list.
     * @param templateName the lookup name of the template in question.
     */
     
    public synchronized void removeAutoInclude(String templateName) {
        autoIncludes.remove(templateName);
    }
    
    /**
     * Returns FreeMarker version.
     */
    public static String getVersionNumber() {
    	return "3.0 Preview";
    }
    
    /**
     * Set whether the getTemplate() methods throw exceptions
     * when there is a (recoverable) parsing problem in the template.
     * This would only be set true by certain tools such as FTL-aware
     * editors that work with FTL code that contains syntactical errors. 
     * @param tolerateParsingProblems
     */
    
    public void setTolerateParsingProblems(boolean tolerateParsingProblems) {
    	this.tolerateParsingProblems = tolerateParsingProblems;
    }
}
