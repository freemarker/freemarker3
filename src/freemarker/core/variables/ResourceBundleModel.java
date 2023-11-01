package freemarker.core.variables;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static freemarker.core.variables.Wrap.asString;
import static freemarker.core.variables.Wrap.wrap;
import static freemarker.core.variables.Wrap.unwrap;

/**
 * <p>
 * A hash model that wraps a resource bundle. Makes it convenient to store
 * localized content in the data model. It also acts as a method model that will
 * take a resource key and arbitrary number of arguments and will apply
 * {@link MessageFormat} with arguments on the string represented by the key.
 * </p>
 *
 * <p>
 * Typical usages:
 * </p>
 * <ul>
 * <li><tt>bundle.resourceKey</tt> will retrieve the object from resource bundle
 * with key <tt>resourceKey</tt></li>
 * <li><tt>bundle("patternKey", arg1, arg2, arg3)</tt> will retrieve the string
 * from resource bundle with key <tt>patternKey</tt>, and will use it as a
 * pattern
 * for MessageFormat with arguments arg1, arg2 and arg3</li>
 * </ul>
 * 
 * @author Attila Szegedi
 * @version $Id: ResourceBundleModel.java,v 1.22 2004/01/06 17:06:42 szegedia
 *          Exp $
 */
public class ResourceBundleModel extends Pojo implements WrappedMethod {
    private Hashtable<String, MessageFormat> formats = null;

    public ResourceBundleModel(ResourceBundle bundle) {
        super(bundle);
    }

    /**
     * Takes first argument as a resource key, looks up a string in resource bundle
     * with this key, then applies a MessageFormat.format on the string with the
     * rest of the arguments. The created MessageFormats are cached for later reuse.
     */
    public Object exec(List<Object> arguments) throws EvaluationException {
        // Must have at least one argument - the key
        if (arguments.size() < 1)
            throw new EvaluationException("No message key was specified");
        // Read it
        Iterator<Object> it = arguments.iterator();
        String key = asString(it.next());
        try {
            if (!it.hasNext()) {
                return wrap((getWrappedObject()).getObject(key));
            }
            // Copy remaining arguments into an Object[]
            int args = arguments.size() - 1;
            Object[] params = new Object[args];
            for (int i = 0; i < args; ++i) {
                params[i] = unwrap(it.next());
            }
            // Invoke format
            return format(key, params);
        } catch (MissingResourceException e) {
            throw new EvaluationException("No such key: " + key);
        } catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    /**
     * Provides direct access to caching format engine from code (instead of from
     * script).
     */
    public String format(String key, Object[] params)
            throws MissingResourceException {
        // Check to see if we already have a cache for message formats
        // and construct it if we don't
        // NOTE: this block statement should be synchronized. However
        // concurrent creation of two caches will have no harmful
        // consequences, and we avoid a performance hit.
        /* synchronized(this) */
        {
            if (formats == null)
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
            if (format == null) {
                format = new MessageFormat(getWrappedObject().getString(key));
                format.setLocale(getWrappedObject().getLocale());
                formats.put(key, format);
            }
        }

        // Perform the formatting. We synchronize on it in case it
        // contains date formatting, which is not thread-safe.
        synchronized (format) {
            return format.format(params);
        }
    }

    public ResourceBundle getWrappedObject() {
        return (ResourceBundle) super.getWrappedObject();
    }
}
