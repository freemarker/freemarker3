package freemarker.core;

import java.beans.Beans;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import freemarker.template.*;
import freemarker.template.utility.StringUtil;
import freemarker.core.ast.ArithmeticEngine;
import freemarker.ext.beans.BeansWrapper;

/**
 * This is a common superclass of {@link freemarker.template.Configuration},
 * {@link freemarker.template.Template}, and {@link Environment} classes.
 * It provides settings that are common to each of them. FreeMarker
 * uses a three-level setting hierarchy - the return value of every setting
 * getter method on <code>Configurable</code> objects inherits its value from its parent 
 * <code>Configurable</code> object, unless explicitly overridden by a call to a 
 * corresponding setter method on the object itself. The parent of an 
 * <code>Environment</code> object is a <code>Template</code> object, the
 * parent of a <code>Template</code> object is a <code>Configuration</code>
 * object.
 *
 * @version $Id: Configurable.java,v 1.24 2006/02/03 19:22:03 revusky Exp $
 * @author Attila Szegedi
 */
public class Configurable
{
    public static final String LOCALE_KEY = "locale";
    public static final String NUMBER_FORMAT_KEY = "number_format";
    public static final String TIME_FORMAT_KEY = "time_format";
    public static final String DATE_FORMAT_KEY = "date_format";
    public static final String DATETIME_FORMAT_KEY = "datetime_format";
    public static final String TIME_ZONE_KEY = "time_zone";
    public static final String TEMPLATE_EXCEPTION_HANDLER_KEY = "template_exception_handler";
    public static final String ARITHMETIC_ENGINE_KEY = "arithmetic_engine";
    public static final String OBJECT_WRAPPER_KEY = "object_wrapper";
    public static final String BOOLEAN_FORMAT_KEY = "boolean_format";
    public static final String OUTPUT_ENCODING_KEY = "output_encoding";
    public static final String URL_ESCAPING_CHARSET_KEY = "url_escaping_charset";
    public static final String STRICT_BEAN_MODELS = "strict_bean_models";

    private static final char COMMA = ',';
    
    private Configurable parent;
    private Properties properties;
    private HashMap<Object, Object> customAttributes;
    
    private Locale locale;
    private String numberFormat;
    private String timeFormat;
    private String dateFormat;
    private String dateTimeFormat;
    private TimeZone timeZone;
    private String trueFormat;
    private String falseFormat;
    private TemplateExceptionHandler templateExceptionHandler;
    private ArithmeticEngine arithmeticEngine;
    private BeansWrapper objectWrapper;
    private String outputEncoding;
    private boolean outputEncodingSet;
    private String urlEscapingCharset;
    private boolean urlEscapingCharsetSet;
    
    public Configurable() {
        parent = null;
        locale = Locale.getDefault();
        timeZone = TimeZone.getDefault();
        numberFormat = "number";
        timeFormat = "";
        dateFormat = "";
        dateTimeFormat = "";
        trueFormat = "true";
        falseFormat = "false";
        templateExceptionHandler = TemplateExceptionHandler.DEBUG_HANDLER;
        arithmeticEngine = ArithmeticEngine.BIGDECIMAL_ENGINE;
        objectWrapper = BeansWrapper.getDefaultInstance();
        // outputEncoding and urlEscapingCharset defaults to null,
        // which means "not specified"
        
        properties = new Properties();
        properties.setProperty(LOCALE_KEY, locale.toString());
        properties.setProperty(TIME_FORMAT_KEY, timeFormat);
        properties.setProperty(DATE_FORMAT_KEY, dateFormat);
        properties.setProperty(DATETIME_FORMAT_KEY, dateTimeFormat);
        properties.setProperty(TIME_ZONE_KEY, timeZone.getID());
        properties.setProperty(NUMBER_FORMAT_KEY, numberFormat);
        properties.setProperty(TEMPLATE_EXCEPTION_HANDLER_KEY, templateExceptionHandler.getClass().getName());
        properties.setProperty(ARITHMETIC_ENGINE_KEY, arithmeticEngine.getClass().getName());
        properties.setProperty(BOOLEAN_FORMAT_KEY, "true,false");
        // as outputEncoding and urlEscapingCharset defaults to null, 
        // they are not set
        
        customAttributes = new HashMap<Object, Object>();
    }
    
    /**
     * Creates a new instance. Normally you do not need to use this constructor,
     * as you don't use <code>Configurable</code> directly, but its subclasses.
     */
    public Configurable(Configurable parent) {
        this.parent = parent;
        locale = null;
        numberFormat = null;
        trueFormat = null;
        falseFormat = null;
        templateExceptionHandler = null;
        properties = new Properties(parent.properties);
        customAttributes = new HashMap<Object, Object>();
    }

    protected Object clone() throws CloneNotSupportedException {
        Configurable copy = (Configurable)super.clone();
        copy.properties = new Properties(properties);
        copy.customAttributes = new HashMap<Object,Object>(customAttributes);
        return copy;
    }
    
    /**
     * Returns the parent <tt>Configurable</tt> object of this object.
     * The parent stores the default values for this configurable. For example,
     * the parent of the {@link freemarker.template.Template} object is the
     * {@link freemarker.template.Configuration} object, so setting values not
     * specfied on template level are specified by the confuration object.
     *
     * @return the parent <tt>Configurable</tt> object, or null, if this is
     *    the root <tt>Configurable</tt> object.
     */
    public final Configurable getParent() {
        return parent;
    }
    
    /**
     * Reparenting support. This is used by Environment when it includes a
     * template - the included template becomes the parent configurable during
     * its evaluation.
     */
    public void setParent(Configurable parent) {
        this.parent = parent;
    }
    
    /**
     * Sets the locale to assume when searching for template files with no 
     * explicit requested locale.
     */
    public void setLocale(Locale locale) {
        if (locale == null) throw new IllegalArgumentException("Setting \"locale\" can't be null");
        this.locale = locale;
        properties.setProperty(LOCALE_KEY, locale.toString());
    }

    /**
     * Returns the time zone to use when formatting time values. Defaults to 
     * system time zone.
     */
    public TimeZone getTimeZone() {
        return timeZone != null ? timeZone : parent.getTimeZone();
    }

    /**
     * Sets the time zone to use when formatting time values. 
     */
    public void setTimeZone(TimeZone timeZone) {
        if (timeZone == null) throw new IllegalArgumentException("Setting \"time_zone\" can't be null");
        this.timeZone = timeZone;
        properties.setProperty(TIME_ZONE_KEY, timeZone.getID());
    }

    /**
     * Returns the assumed locale when searching for template files with no
     * explicit requested locale. Defaults to system locale.
     */
    public Locale getLocale() {
        return locale != null ? locale : parent.getLocale();
    }

    /**
     * Sets the number format used to convert numbers to strings.
     */
    public void setNumberFormat(String numberFormat) {
        if (numberFormat == null) throw new IllegalArgumentException("Setting \"number_format\" can't be null");
        this.numberFormat = numberFormat;
        properties.setProperty(NUMBER_FORMAT_KEY, numberFormat);
    }

    /**
     * Returns the default number format used to convert numbers to strings.
     * Defaults to <tt>"number"</tt>
     */
    public String getNumberFormat() {
        return numberFormat != null ? numberFormat : parent.getNumberFormat();
    }

    public void setBooleanFormat(String booleanFormat) {
        if (booleanFormat == null) {
            throw new IllegalArgumentException("Setting \"boolean_format\" can't be null");
        } 
        int comma = booleanFormat.indexOf(COMMA);
        if(comma == -1) {
            throw new IllegalArgumentException("Setting \"boolean_format\" must consist of two comma-separated values for true and false respectively");
        }
        trueFormat = booleanFormat.substring(0, comma);
        falseFormat = booleanFormat.substring(comma + 1);
        properties.setProperty(BOOLEAN_FORMAT_KEY, booleanFormat);
    }
    
    public String getBooleanFormat() {
        if(trueFormat == null) {
            return parent.getBooleanFormat(); 
        }
        return trueFormat + COMMA + falseFormat;
    }
    
    public String getBooleanFormat(boolean value) {
        return value ? getTrueFormat() : getFalseFormat(); 
    }
    
    private String getTrueFormat() {
        return trueFormat != null ? trueFormat : parent.getTrueFormat(); 
    }
    
    private String getFalseFormat() {
        return falseFormat != null ? falseFormat : parent.getFalseFormat(); 
    }

    /**
     * Sets the date format used to convert date models representing time-only
     * values to strings.
     */
    public void setTimeFormat(String timeFormat) {
        if (timeFormat == null) throw new IllegalArgumentException("Setting \"time_format\" can't be null");
        this.timeFormat = timeFormat;
        properties.setProperty(TIME_FORMAT_KEY, timeFormat);
    }

    /**
     * Returns the date format used to convert date models representing
     * time-only dates to strings.
     * Defaults to <tt>"time"</tt>
     */
    public String getTimeFormat() {
        return timeFormat != null ? timeFormat : parent.getTimeFormat();
    }

    /**
     * Sets the date format used to convert date models representing date-only
     * dates to strings.
     */
    public void setDateFormat(String dateFormat) {
        if (dateFormat == null) throw new IllegalArgumentException("Setting \"date_format\" can't be null");
        this.dateFormat = dateFormat;
        properties.setProperty(DATE_FORMAT_KEY, dateFormat);
    }

    /**
     * Returns the date format used to convert date models representing 
     * date-only dates to strings.
     * Defaults to <tt>"date"</tt>
     */
    public String getDateFormat() {
        return dateFormat != null ? dateFormat : parent.getDateFormat();
    }

    /**
     * Sets the date format used to convert date models representing datetime
     * dates to strings.
     */
    public void setDateTimeFormat(String dateTimeFormat) {
        if (dateTimeFormat == null) throw new IllegalArgumentException("Setting \"datetime_format\" can't be null");
        this.dateTimeFormat = dateTimeFormat;
        properties.setProperty(DATETIME_FORMAT_KEY, dateTimeFormat);
    }

    /**
     * Returns the date format used to convert date models representing datetime
     * dates to strings.
     * Defaults to <tt>"datetime"</tt>
     */
    public String getDateTimeFormat() {
        return dateTimeFormat != null ? dateTimeFormat : parent.getDateTimeFormat();
    }

    /**
     * Sets the exception handler used to handle template exceptions. 
     *
     * @param templateExceptionHandler the template exception handler to use for 
     * handling {@link TemplateException}s. By default, 
     * {@link TemplateExceptionHandler#HTML_DEBUG_HANDLER} is used.
     */
    public void setTemplateExceptionHandler(TemplateExceptionHandler templateExceptionHandler) {
        if (templateExceptionHandler == null) throw new IllegalArgumentException("Setting \"template_exception_handler\" can't be null");
        this.templateExceptionHandler = templateExceptionHandler;
        properties.setProperty(TEMPLATE_EXCEPTION_HANDLER_KEY, templateExceptionHandler.getClass().getName());
    }

    /**
     * Retrieves the exception handler used to handle template exceptions. 
     */
    public TemplateExceptionHandler getTemplateExceptionHandler() {
        return templateExceptionHandler != null
                ? templateExceptionHandler : parent.getTemplateExceptionHandler();
    }

    /**
     * Sets the arithmetic engine used to perform arithmetic operations.
     *
     * @param arithmeticEngine the arithmetic engine used to perform arithmetic
     * operations.By default, {@link ArithmeticEngine#BIGDECIMAL_ENGINE} is 
     * used.
     */
    public void setArithmeticEngine(ArithmeticEngine arithmeticEngine) {
        if (arithmeticEngine == null) throw new IllegalArgumentException("Setting \"arithmetic_engine\" can't be null");
        this.arithmeticEngine = arithmeticEngine;
        properties.setProperty(ARITHMETIC_ENGINE_KEY, arithmeticEngine.getClass().getName());
    }

    /**
     * Retrieves the arithmetic engine used to perform arithmetic operations.
     */
    public ArithmeticEngine getArithmeticEngine() {
        return arithmeticEngine != null
                ? arithmeticEngine : parent.getArithmeticEngine();
    }

    /**
     * Sets the object wrapper used to wrap objects to template models.
     *
     * @param objectWrapper the object wrapper used to wrap objects to template
     * models.
     */
    public void setObjectWrapper(BeansWrapper objectWrapper) {
        if (objectWrapper == null) throw new IllegalArgumentException("Setting \"object_wrapper\" can't be null");
        this.objectWrapper = objectWrapper;
        properties.setProperty(OBJECT_WRAPPER_KEY, objectWrapper.getClass().getName());
    }

    /**
     * Retrieves the object wrapper used to wrap objects to template models.
     */
    public BeansWrapper getObjectWrapper() {
        return BeansWrapper.getDefaultInstance();
//        return objectWrapper != null
//                ? objectWrapper : parent.getObjectWrapper();
    }
    
    /**
     * Sets the output encoding. Allows <code>null</code>, which means that the
     * output encoding is not known.
     */
    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
        // java.util.Properties doesn't allow null value!
        if (outputEncoding != null) {
            properties.setProperty(OUTPUT_ENCODING_KEY, outputEncoding);
        } else {
            properties.remove(OUTPUT_ENCODING_KEY);
        }
        outputEncodingSet = true;
    }
    
    public void setNumbersForComputers(boolean b) {
    	
    }
    
    public String getOutputEncoding() {
        return outputEncodingSet
                ? outputEncoding
                : (parent != null ? parent.getOutputEncoding() : null);
    }
    
    /**
     * Sets the URL escaping charset. Allows <code>null</code>, which means that the
     * output encoding will be used for URL escaping.
     */
    public void setURLEscapingCharset(String urlEscapingCharset) {
        this.urlEscapingCharset = urlEscapingCharset;
        // java.util.Properties doesn't allow null value!
        if (urlEscapingCharset != null) {
            properties.setProperty(URL_ESCAPING_CHARSET_KEY, urlEscapingCharset);
        } else {
            properties.remove(URL_ESCAPING_CHARSET_KEY);
        }
        urlEscapingCharsetSet = true;
    }
    
    public String getURLEscapingCharset() {
        return urlEscapingCharsetSet
                ? urlEscapingCharset
                : (parent != null ? parent.getURLEscapingCharset() : null);
    }
    
    /**
     * Sets a setting by a name and string value.
     * 
     * <p>List of supported names and their valid values:
     * <ul>
     *   <li><code>"locale"</code>: local codes with the usual format, such as <code>"en_US"</code>.
     *   <li><code>"template_exception_handler"</code>:  If the value contains dot, then it is
     *       interpreted as class name, and the object will be created with
     *       its parameterless constructor. If the value does not contain dot,
     *       then it must be one of these special values:
     *       <code>"rethrow"</code>, <code>"debug"</code>,
     *       <code>"html_debug"</code>, <code>"ignore"</code> (case insensitive).
     *   <li><code>"arithmetic_engine"</code>: If the value contains dot, then it is
     *       interpreted as class name, and the object will be created with
     *       its parameterless constructor. If the value does not contain dot,
     *       then it must be one of these special values:
     *       <code>"bigdecimal"</code>, <code>"conservative"</code> (case insensitive).  
     *   <li><code>"object_wrapper"</code>: If the value contains dot, then it is
     *       interpreted as class name, and the object will be created with
     *       its parameterless constructor. If the value does not contain dot,
     *       then it must be one of these special values:
     *       <code>"simple"</code>, <code>"beans"</code>.
     *   <li><code>"number_format"</code>: pattern as <code>java.text.DecimalFormat</code> defines.
     *   <li><code>"boolean_format"</code>: the textual value for boolean true and false,
     *       separated with comma. For example <code>"yes,no"</code>.
     *   <li><code>"date_format", "time_format", "datetime_format"</code>: patterns as
     *       <code>java.text.SimpleDateFormat</code> defines.
     *   <li><code>"time_zone"</code>: time zone, with the format as
     *       <code>java.util.TimeZone.getTimeZone</code> defines. For example <code>"GMT-8:00"</code> or
     *       <code>"America/Los_Angeles"</code>
     *   <li><code>"output_encoding"</code>: Informs FreeMarker about the charset
     *       used for the output. As FreeMarker outputs character stream (not
     *       byte stream), it is not aware of the output charset unless the
     *       software that encloses it tells it explicitly with this setting.
     *       Some templates may use FreeMarker features that require this.</code>
     *   <li><code>"url_escaping_charset"</code>: If this setting is set, then it
     *       overrides the value of the <code>"output_encoding"</code> setting when
     *       FreeMarker does URL encoding.
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
        try {
            if (LOCALE_KEY.equals(key)) {
                setLocale(StringUtil.deduceLocale(value));
            } else if (NUMBER_FORMAT_KEY.equals(key)) {
                setNumberFormat(value);
            } else if (TIME_FORMAT_KEY.equals(key)) {
                setTimeFormat(value);
            } else if (DATE_FORMAT_KEY.equals(key)) {
                setDateFormat(value);
            } else if (DATETIME_FORMAT_KEY.equals(key)) {
                setDateTimeFormat(value);
            } else if (TIME_ZONE_KEY.equals(key)) {
                setTimeZone(TimeZone.getTimeZone(value));
            } else if (TEMPLATE_EXCEPTION_HANDLER_KEY.equals(key)) {
                if (value.indexOf('.') == -1) {
                    if ("debug".equalsIgnoreCase(value)) {
                        setTemplateExceptionHandler(
                                TemplateExceptionHandler.DEBUG_HANDLER);
                    } else if ("html_debug".equalsIgnoreCase(value)) {
                        setTemplateExceptionHandler(
                                TemplateExceptionHandler.HTML_DEBUG_HANDLER);
                    } else if ("ignore".equalsIgnoreCase(value)) {
                        setTemplateExceptionHandler(
                                TemplateExceptionHandler.IGNORE_HANDLER);
                    } else if ("rethrow".equalsIgnoreCase(value)) {
                        setTemplateExceptionHandler(
                                TemplateExceptionHandler.RETHROW_HANDLER);
                    } else {
                        throw invalidSettingValueException(key, value);
                    }
                } else {
                    setTemplateExceptionHandler(
                            (TemplateExceptionHandler) Class.forName(value)
                            .newInstance());
                }
            } else if (ARITHMETIC_ENGINE_KEY.equals(key)) {
                if (value.indexOf('.') == -1) { 
                    if ("bigdecimal".equalsIgnoreCase(value)) {
                        setArithmeticEngine(ArithmeticEngine.BIGDECIMAL_ENGINE);
                    } else if ("conservative".equalsIgnoreCase(value)) {
                        setArithmeticEngine(ArithmeticEngine.CONSERVATIVE_ENGINE);
                    } else {
                        throw invalidSettingValueException(key, value);
                    }
                } else {
                    setArithmeticEngine(
                            (ArithmeticEngine) Class.forName(value)
                            .newInstance());
                }
            } else if (OBJECT_WRAPPER_KEY.equals(key)) {
                if (value.indexOf('.') == -1) {
                    setObjectWrapper(new BeansWrapper());
                } else {
                    setObjectWrapper((BeansWrapper) Class.forName(value)
                            .newInstance());
                }
            } else if (BOOLEAN_FORMAT_KEY.equals(key)) {
                setBooleanFormat(value);
            } else if (OUTPUT_ENCODING_KEY.equals(key)) {
                setOutputEncoding(value);
            } else if (URL_ESCAPING_CHARSET_KEY.equals(key)) {
                setURLEscapingCharset(value);
            } else if (STRICT_BEAN_MODELS.equals(key)) {
            	setStrictBeanModels(StringUtil.getYesNo(value));
            }
            else {
                throw unknownSettingException(key);
            }
        } catch(TemplateException e) {
            throw e;
        } catch(Exception e) {
            throw new TemplateException(
                    "Failed to set setting " + key + " to value " + value,
                    e, getEnvironment());
        }
    }
    
    public void setStrictBeanModels(boolean strict) {
    	if (!(objectWrapper instanceof BeansWrapper)) {
    		throw new IllegalStateException("Not a beans wrapper");
    	}
    	((BeansWrapper) objectWrapper).setStrict(strict);
    }
    
    /**
     * Returns the textual representation of a setting.
     * @param key the setting key. Can be any of standard <tt>XXX_KEY</tt>
     * constants, or a custom key.
     *
     * @deprecated This method was always defective, and certainly it always
     *     will be. Don't use it. (Simply, it's hardly possible in general to
     *     convert setting values to text in a way that ensures that
     *     {@link #setSetting(String, String)} will work with them correctly.)
     */
    public String getSetting(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * This meant to return the String-to-String <code>Map</code> of the
     * settings. So it actually should return a <code>Properties</code> object,
     * but it doesn't by mistake. The returned <code>Map</code> is read-only,
     * but it will reflect the further configuration changes (aliasing effect).
     *
     * @deprecated This method was always defective, and certainly it always
     *     will be. Don't use it. (Simply, it's hardly possible in general to
     *     convert setting values to text in a way that ensures that
     *     {@link #setSettings(Properties)} will work with them correctly.)
     */
    public Map getSettings() {
        return Collections.unmodifiableMap(properties);
    }
    
    public Environment getEnvironment() {
        return Environment.getCurrentEnvironment();
    }
    
    protected TemplateException unknownSettingException(String name) {
        return new UnknownSettingException(name, getEnvironment());
    }

    protected TemplateException invalidSettingValueException(String name, String value) {
        return new TemplateException("Invalid value for setting " + name + ": " + value, getEnvironment());
    }
    
    public static class UnknownSettingException extends TemplateException {
        private static final long serialVersionUID = 6904771126094186066L;

        private UnknownSettingException(String name, Environment env) {
            super("Unknown setting: " + name, env);
        }
    }

    /**
     * Set the settings stored in a <code>Properties</code> object.
     * 
     * @throws TemplateException if the <code>Properties</code> object contains
     *     invalid keys, or invalid setting values, or any other error occurs
     *     while changing the settings.
     */    
    public void setSettings(Properties props) {
        Iterator<Object> it = props.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            setSetting(key, props.getProperty(key).trim()); 
        }
    }
    
    /**
     * Reads a setting list (key and element pairs) from the input stream.
     * The stream has to follow the usual <code>.properties</code> format.
     *
     * @throws TemplateException if the stream contains
     *     invalid keys, or invalid setting values, or any other error occurs
     *     while changing the settings.
     * @throws IOException if an error occurred when reading from the input stream.
     */
    public void setSettings(InputStream propsIn) throws TemplateException, IOException {
        Properties p = new Properties();
        p.load(propsIn);
        setSettings(p);
    }

    /**
     * Internal entry point for setting unnamed custom attributes
     */
    void setCustomAttribute(Object key, Object value) {
        synchronized(customAttributes) {
            customAttributes.put(key, value);
        }
    }

    /**
     * Internal entry point for getting unnamed custom attributes
     */
    Object getCustomAttribute(Object key, CustomAttribute attr) {
        synchronized(customAttributes) {
            Object o = customAttributes.get(key);
            if(o == null && !customAttributes.containsKey(key)) {
                o = attr.create();
                customAttributes.put(key, o);
            }
            return o;
        }
    }
    
    /**
     * Sets a named custom attribute for this configurable.
     *
     * @param name the name of the custom attribute
     * @param value the value of the custom attribute. You can set the value to
     * null, however note that there is a semantic difference between an
     * attribute set to null and an attribute that is not present, see
     * {@link #removeCustomAttribute(String)}.
     */
    public void setCustomAttribute(String name, Object value) {
        synchronized(customAttributes) {
            customAttributes.put(name, value);
        }
    }
    
    /**
     * Returns an array with names of all custom attributes defined directly 
     * on this configurable. (That is, it doesn't contain the names of custom attributes
     * defined indirectly on its parent configurables.) The returned array is never null,
     * but can be zero-length.
     * The order of elements in the returned array is not defined and can change
     * between invocations.  
     */
    public String[] getCustomAttributeNames() {
        synchronized(customAttributes) {
            ArrayList<String> names = new ArrayList<String>(customAttributes.size());
            for (Object key : customAttributes.keySet()) {
            	if (key instanceof String) {
            		names.add((String) key);
            	}
            }
            String[] arr = new String[names.size()];
            return names.toArray(arr);
        }
    }
    
    /**
     * Removes a named custom attribute for this configurable. Note that this
     * is different than setting the custom attribute value to null. If you
     * set the value to null, {@link #getCustomAttribute(String)} will return
     * null, while if you remove the attribute, it will return the value of
     * the attribute in the parent configurable (if there is a parent 
     * configurable, that is). 
     *
     * @param name the name of the custom attribute
     */
    public void removeCustomAttribute(String name) {
        synchronized(customAttributes) {
            customAttributes.remove(name);
        }
    }

    /**
     * Retrieves a named custom attribute for this configurable. If the 
     * attribute is not present in the configurable, and the configurable has
     * a parent, then the parent is looked up as well.
     *
     * @param name the name of the custom attribute
     *
     * @return the value of the custom attribute. Note that if the custom attribute
     * was created with <tt>&lt;#ftl&nbsp;attributes={...}></tt>, then this value is already
     * unwrapped (i.e. it's a <code>String</code>, or a <code>List</code>, or a
     * <code>Map</code>, ...etc., not a FreeMarker specific class).
     */
    public Object getCustomAttribute(String name) {
        Object retval;
        synchronized(customAttributes) {
            retval = customAttributes.get(name);
            if(retval == null && customAttributes.containsKey(name)) {
                return null;
            }
        }
        if(retval == null && parent != null) {
            return parent.getCustomAttribute(name);
        }
        return retval;
    }
    
    protected void doAutoImportsAndIncludes(Environment env)
    throws TemplateException, IOException
    {
        if(parent != null) parent.doAutoImportsAndIncludes(env);
    }
}
