package freemarker.core;

import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;

/**
 * This class is used to defer retrieval of our protection domain until it is
 * absolutely necessary. This way, when running under secured JVM, but unsecured
 * FM configuration, we don't need the "getProtectionDomain" permission.
 * @author Attila Szegedi
 * @version $Id: $
 */
class DefaultCodeSourceProvider {
    static final CodeSource DEFAULT_CODE_SOURCE = getDefaultCodeSource();
    
    private static final CodeSource getDefaultCodeSource() {
        try {
            return AccessController.doPrivileged(
                    new PrivilegedAction<CodeSource>() {
                        public CodeSource run() {
                            return DefaultCodeSourceProvider.class.getProtectionDomain().getCodeSource();
                        } 
            });
        } catch(SecurityException e) {
            // We'd use this for an optimization only, so if we can't put our 
            // hands on it, don't break sweat
            return null;
        }
        
    }
}
