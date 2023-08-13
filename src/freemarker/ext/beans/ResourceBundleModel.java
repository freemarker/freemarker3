package freemarker.ext.beans;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import static freemarker.ext.beans.ObjectWrapper.wrap;

/**
 * <p>A hash model that wraps a resource bundle. Makes it convenient to store
 * localized content in the data model. It also acts as a method model that will
 * take a resource key and arbitrary number of arguments and will apply
 * {@link MessageFormat} with arguments on the string represented by the key.</p>
 *
 * <p>Typical usages:</p>
 * <ul>
 * <li><tt>bundle.resourceKey</tt> will retrieve the object from resource bundle
 * with key <tt>resourceKey</tt></li>
 * <li><tt>bundle("patternKey", arg1, arg2, arg3)</tt> will retrieve the string
 * from resource bundle with key <tt>patternKey</tt>, and will use it as a pattern
 * for MessageFormat with arguments arg1, arg2 and arg3</li>
 * </ul>
 * @author Attila Szegedi
 * @version $Id: ResourceBundleModel.java,v 1.22 2004/01/06 17:06:42 szegedia Exp $
 */
public class ResourceBundleModel extends Pojo implements TemplateMethodModelEx
{
    private Hashtable<String, MessageFormat> formats = null;

    public ResourceBundleModel(ResourceBundle bundle)
    {
        super(bundle);
    }

    /**
     * Overridden to invoke the getObject method of the resource bundle.
     */
    protected Object invokeGenericGet(Map keyMap, String key) 
    {
        try
        {
            return wrap(((ResourceBundle)object).getObject(key));
        }
        catch(MissingResourceException e)
        {
            throw new TemplateModelException("No such key: " + key);
        }
    }

    /**
     * Returns true if this bundle contains no objects.
     */
    public boolean isEmpty()
    {
        return !((ResourceBundle)object).getKeys().hasMoreElements() &&
            super.isEmpty();
    }

    public int size()
    {
        return keySet().size();
    }

    protected Set keySet()
    {
        Set set = super.keySet();
        Enumeration e = ((ResourceBundle)object).getKeys();
        while (e.hasMoreElements()) {
            set.add(e.nextElement());
        }
        return set;
    }

    /**
     * Takes first argument as a resource key, looks up a string in resource bundle
     * with this key, then applies a MessageFormat.format on the string with the
     * rest of the arguments. The created MessageFormats are cached for later reuse.
     */
    public Object exec(List arguments)
        throws
        TemplateModelException
    {
        // Must have at least one argument - the key
        if(arguments.size() < 1)
            throw new TemplateModelException("No message key was specified");
        // Read it
        Iterator it = arguments.iterator();
        String key = unwrap((TemplateModel)it.next()).toString();
        try
        {
            if(!it.hasNext())
            {
                return wrap(((ResourceBundle)object).getObject(key));
            }
    
            // Copy remaining arguments into an Object[]
            int args = arguments.size() - 1;
            Object[] params = new Object[args];
            for(int i = 0; i < args; ++i)
                params[i] = unwrap((TemplateModel)it.next());
    
            // Invoke format
            return format(key, params);
        }
        catch(MissingResourceException e)
        {
            throw new TemplateModelException("No such key: " + key);
        }
        catch(Exception e)
        {
            throw new TemplateModelException(e.getMessage());
        }
    }

    /**
     * Provides direct access to caching format engine from code (instead of from script).
     */
    public String format(String key, Object[] params)
        throws
        MissingResourceException
    {
        // Check to see if we already have a cache for message formats
        // and construct it if we don't
        // NOTE: this block statement should be synchronized. However
        // concurrent creation of two caches will have no harmful
        // consequences, and we avoid a performance hit.
        /* synchronized(this) */
        {
            if(formats == null)
                formats = new Hashtable<String, MessageFormat>();
        }

        MessageFormat format = null;
        // Check to see if we already have a requested MessageFormat cached
        // and construct it if we don't
        // NOTE: this block statement should be synchronized. However
        // concurrent creation of two formats will have no harmful
        // consequences, and we avoid a performance hit.
        /* synchronized(formats) */
        {
            format = formats.get(key);
            if(format == null)
            {
                format = new MessageFormat(((ResourceBundle)object).getString(key));
                format.setLocale(getBundle().getLocale());
                formats.put(key, format);
            }
        }

        // Perform the formatting. We synchronize on it in case it
        // contains date formatting, which is not thread-safe.
        synchronized(format)
        {
            return format.format(params);
        }
    }

    public ResourceBundle getBundle()
    {
        return (ResourceBundle)object;
    }
}
