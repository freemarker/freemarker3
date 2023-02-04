package freemarker.template.utility;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import freemarker.log.Logger;

/**
 * @author Attila Szegedi
 * @version $Id: SecurityUtilities.java,v 1.2 2004/11/11 13:27:39 szegedia Exp $
 */
public class SecurityUtilities
{
    private static final Logger logger = Logger.getLogger("freemarker.security");
    private SecurityUtilities()
    {
    }
    
    public static String getSystemProperty(final String key)
    {
        return AccessController.doPrivileged(
            new PrivilegedAction<String>()
            {
                public String run()
                {
                    return System.getProperty(key);
                }
            });
    }

    public static String getSystemProperty(final String key, final String defValue)
    {
        try
        {
            return AccessController.doPrivileged(
                new PrivilegedAction<String>()
                {
                    public String run()
                    {
                        return System.getProperty(key, defValue);
                    }
                });
        }
        catch(AccessControlException e)
        {
            logger.warn("Insufficient permissions to read system property '" + 
                    key + "', using default value '" + defValue + "'");
            return defValue;
        }
    }

    public static Integer getSystemProperty(final String key, final int defValue)
    {
        try
        {
            return AccessController.doPrivileged(
                new PrivilegedAction<Integer>()
                {
                    public Integer run()
                    {
                        return Integer.getInteger(key, defValue);
                    }
                });
        }
        catch(AccessControlException e)
        {
            logger.warn("Insufficient permissions to read system property '" + 
                    key + "', using default value " + defValue + "");
            return Integer.valueOf(defValue);
        }
    }
}
