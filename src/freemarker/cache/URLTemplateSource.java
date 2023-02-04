package freemarker.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Wraps a <code>java.net.URL</code>, and implements methods required for a typical template source.
 * @version $Id: URLTemplateSource.java,v 1.4 2003/04/02 11:43:18 szegedia Exp $
 * @author Daniel Dekany
 */
class URLTemplateSource {
    private final URL url;
    private URLConnection conn;
    private InputStream inputStream;

    URLTemplateSource(URL url) throws IOException {
        this.url = url;
        this.conn = url.openConnection();
    }

    public boolean equals(Object o) {
        if (o instanceof URLTemplateSource) {
            return url.equals(((URLTemplateSource) o).url);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return url.hashCode();
    }

    public String toString() {
        return url.toString();
    }
    
    long lastModified() {
        if (conn instanceof JarURLConnection) {
            // There is a bug in sun's jar url connection that causes file handle leaks when calling getLastModified()
            // Since the time stamps of jar file contents can't vary independent from the jar file timestamp, just use
            // the jar file timestamp
            URL jarURL=((JarURLConnection)conn).getJarFileURL();
            if (jarURL.getProtocol().equals("file")) {
              // Return the last modified time of the underlying file - saves some opening and closing
              return new File(jarURL.getFile()).lastModified();
            } else {
              // Use the URL mechanism
              URLConnection jarConn=null;
              try {
                jarConn=jarURL.openConnection();
                return jarConn.getLastModified();
              } catch (IOException e) {
                return -1;
              } finally {
                try {
                  if (jarConn!=null) jarConn.getInputStream().close();
                } catch (IOException e) { }
              }
            }
          } else {
            long lastModified = conn.getLastModified();
            if (lastModified == -1L && url.getProtocol().equals("file")) {
                // Hack for obtaining accurate last modified time for
                // URLs that point to the local file system. This is fixed
                // in JDK 1.4, but prior JDKs returns -1 for file:// URLs.
                return new File(url.getFile()).lastModified();
            } else {
                return lastModified;
            }
          }
    }

    InputStream getInputStream() throws IOException {
        inputStream = conn.getInputStream();
        return inputStream;
    }

    void close() throws IOException {
        try {
          if (inputStream != null) {
              inputStream.close();
          }
        } finally {
          inputStream = null;
          conn = null;
        }
    }
    
    /**
     * For jar: URLs, returns a code source that points to the URL of the JAR
     * file as the code source URL. If the JAR file is signed, the code source
     * will contain the appropriate certificates as well. For other URLs, 
     * returns the code source with URL itself and no certificates.
     * @return an appropriate CodeSource for this template source.
     * @throws IOException
     */
    CodeSource getCodeSource() throws IOException {
        Certificate[] signers;
        URL baseUrl;
        if(url.getProtocol().equals("jar")) {
            String sUrl = url.toExternalForm();
            int bang = sUrl.indexOf('!');
            if(bang != -1) {
                baseUrl = new URL(sUrl.substring(4, bang));
                URL jarUrl = new URL(sUrl.substring(0, bang));
                JarFile f = ((JarURLConnection)jarUrl.openConnection()).getJarFile();
                try
                {
                    JarEntry entry = f.getJarEntry(sUrl.substring(bang + 1));
                    if(entry != null) {
                        signers = entry.getCertificates();
                    }
                    else {
                        signers = null;
                    }
                } finally {
                    f.close();
                }
            } else {
                baseUrl = new URL(sUrl.substring(4));
                signers = null;
            }
        } else {
            baseUrl = url;
            signers = null;
        }
        return new CodeSource(baseUrl, signers); 
    }
}
