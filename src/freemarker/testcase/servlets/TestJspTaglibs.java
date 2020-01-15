/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package freemarker.testcase.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.template.TemplateException;
import freemarker.testcase.TemplateTestCase;
import junit.framework.TestCase;

/**
 * @version $Id: TestJspTaglibs.java,v 1.14 2005/10/26 17:57:03 revusky Exp $
 * @author Attila Szegedi
 */
public class TestJspTaglibs extends TestCase {
    
    File refFile;
    private File outputDir;
    
    public TestJspTaglibs(String name) {
        super(name);
    }
    
    public TestJspTaglibs(String name, String filename) {
        super(name);
    }

    public void setUp() throws Exception {
        URL url = TestJspTaglibs.class.getResource("../testcases.xml");
        File thisDir = new File(new File(url.getFile()).getParentFile(), "servlets");
        refFile = new File(thisDir, "reference/test-jsptaglibs.txt");
        outputDir = new File(thisDir, "reference");
    }

    public void runTest() throws TemplateException {
        try {
            ServletConfig cfg = new MockServletConfig();
            FreemarkerServlet servlet = new FreemarkerServlet();
            servlet.init(cfg);
            MockRequest req = new MockRequest("test-jsptaglibs.txt");
            MockResponse resp = new MockResponse();
            servlet.doGet(req, resp);
            String strResp = resp.toString();
            StringReader output = new StringReader(strResp);
            File outFile = new File (outputDir, "test-jsptaglibs.txt.out");
            FileWriter fw = new FileWriter(outFile);
            fw.write(strResp);
            fw.close();
            Reader reference = new FileReader(refFile);
            TemplateTestCase.compare(reference, output, refFile, outFile);
//            showTestResults( referenceText, resp.toString() );
        }
        catch(Exception e) {
            e.printStackTrace();
            throw new TemplateException(e, null);
        }
    }

    private static class MockServletConfig
        implements ServletConfig, ServletContext {
        private final Properties initParams = new Properties();
        private final Hashtable attributes = new Hashtable();

        MockServletConfig() {
            initParams.setProperty("TemplatePath", "/template/");
            initParams.setProperty("NoCache", "true");
            initParams.setProperty("TemplateUpdateInterval", "0");
            initParams.setProperty("DefaultEncoding", "UTF-8");
            initParams.setProperty("ObjectWrapper", "beans");
        }

        public String getInitParameter(String name) {
            return initParams.getProperty(name);
        }

        public Enumeration getInitParameterNames() {
            return initParams.keys();
        }

        public ServletContext getServletContext() {
            return this;
        }

        public String getServletName() {
            return "freemarker";
        }

        public Object getAttribute(String arg0) {
            return attributes.get(arg0);
        }

        public Enumeration getAttributeNames() {
            return attributes.keys();
        }

        public ServletContext getContext(String arg0) {
            throw new UnsupportedOperationException();
        }
        
        public String getContextPath() {
            throw new UnsupportedOperationException();
        }

        public int getMajorVersion() {
            return 0;
        }

        public String getMimeType(String arg0) {
            throw new UnsupportedOperationException();
        }

        public int getMinorVersion() {
            return 0;
        }

        public RequestDispatcher getNamedDispatcher(String arg0) {
            throw new UnsupportedOperationException();
        }

        public String getRealPath(String arg0) {
            return null;
        }

        public RequestDispatcher getRequestDispatcher(String arg0) {
            throw new UnsupportedOperationException();
        }

        public URL getResource(String url) {
            if (url.startsWith("/")) {
                url = url.substring(1);
            }
            return getClass().getResource(url);
        }

        public InputStream getResourceAsStream(String url) {
            if (url.startsWith("/")) {
                url = url.substring(1);
            }
            return getClass().getResourceAsStream(url);
        }

        public Set getResourcePaths(String path) {
            if(path.equals("/WEB-INF/lib")) {
                return Collections.singleton("/WEB-INF/lib/taglib-foo.jar");
            }
            else {
                return Collections.EMPTY_SET;
            }
        }

        public String getServerInfo() {
            return "FreeMarker/JUnit";
        }

        /**
         * @deprecated
         */
        public Servlet getServlet(String arg0) {
            throw new UnsupportedOperationException();
        }

        public String getServletContextName() {
            return "freemarker";
        }

        /**
         * @deprecated
         */
        public Enumeration getServletNames() {
            throw new UnsupportedOperationException();
        }

        /**
         * @deprecated
         */
        public Enumeration getServlets() {
            throw new UnsupportedOperationException();
        }

        /**
         * @deprecated
         */
        public void log(Exception arg0, String arg1) {
        }

        public void log(String arg0, Throwable arg1) {
        }

        public void log(String arg0) {
        }

        public void removeAttribute(String arg0) {
            attributes.remove(arg0);
        }

        public void setAttribute(String arg0, Object arg1) {
            attributes.put(arg0, arg1);
        }

		public Dynamic addFilter(String arg0, String arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		public Dynamic addFilter(String arg0, Filter arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		public Dynamic addFilter(String arg0, Class<? extends Filter> arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		public void addListener(String arg0) {
			// TODO Auto-generated method stub
			
		}

		public <T extends EventListener> void addListener(T arg0) {
			// TODO Auto-generated method stub
			
		}

		public void addListener(Class<? extends EventListener> arg0) {
			// TODO Auto-generated method stub
			
		}

		public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, String arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, Servlet arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, Class<? extends Servlet> arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		public <T extends Filter> T createFilter(Class<T> arg0) throws ServletException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T extends EventListener> T createListener(Class<T> arg0) throws ServletException {
			// TODO Auto-generated method stub
			return null;
		}

		public <T extends Servlet> T createServlet(Class<T> arg0) throws ServletException {
			// TODO Auto-generated method stub
			return null;
		}

		public void declareRoles(String... arg0) {
			// TODO Auto-generated method stub
			
		}

		public ClassLoader getClassLoader() {
			// TODO Auto-generated method stub
			return null;
		}

		public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
			// TODO Auto-generated method stub
			return null;
		}

		public int getEffectiveMajorVersion() {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getEffectiveMinorVersion() {
			// TODO Auto-generated method stub
			return 0;
		}

		public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
			// TODO Auto-generated method stub
			return null;
		}

		public FilterRegistration getFilterRegistration(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
			// TODO Auto-generated method stub
			return null;
		}

		public JspConfigDescriptor getJspConfigDescriptor() {
			// TODO Auto-generated method stub
			return null;
		}

		public ServletRegistration getServletRegistration(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public Map<String, ? extends ServletRegistration> getServletRegistrations() {
			// TODO Auto-generated method stub
			return null;
		}

		public SessionCookieConfig getSessionCookieConfig() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean setInitParameter(String arg0, String arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		public void setSessionTrackingModes(Set<SessionTrackingMode> arg0) {
			// TODO Auto-generated method stub
			
		}
    }

    private static final class MockRequest
    implements 
        HttpServletRequest
    {
        private final String pathInfo;
        private HttpSession session;
                   
        MockRequest(String pathInfo) {
            this.pathInfo = pathInfo;
        }

        public String getAuthType() {
            return null;
        }

        public String getContextPath() {
            return null;
        }

        public Cookie[] getCookies() {
            return null;
        }

        public long getDateHeader(String arg0) {
            return 0;
        }

        public String getHeader(String arg0) {
            return null;
        }

        public Enumeration getHeaderNames() {
            return null;
        }

        public Enumeration getHeaders(String arg0) {
            return null;
        }

        public int getIntHeader(String arg0) {
            return 0;
        }

        public String getLocalAddr() {
            return null;
        }

        public String getLocalName() {
            return null;
        }

        public int getLocalPort() {
            return 80; //???
        }

        public int getRemotePort() {
            return 80; //??
        }

        public String getMethod() {
            return null;
        }

        public String getPathInfo() {
            return pathInfo;
        }

        public String getPathTranslated() {
            return null;
        }

        public String getQueryString() {
            return null;
        }

        public String getRemoteUser() {
            return null;
        }

        public String getRequestedSessionId() {
            return null;
        }

        public String getRequestURI() {
            return null;
        }

        public StringBuffer getRequestURL() {
            return null;
        }

        public String getServletPath() {
            return null;
        }

        public HttpSession getSession() {
            return getSession(true);
        }

        public HttpSession getSession(boolean arg0) {
            if(session == null && arg0) session = new MockSession();
            return session;
        }

        public Principal getUserPrincipal() {
            return null;
        }

        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        /**
         * @deprecated
         */
        public boolean isRequestedSessionIdFromUrl() {
            return false;
        }

        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        public boolean isRequestedSessionIdValid() {
            return false;
        }

        public boolean isUserInRole(String arg0) {
            return false;
        }

        public Object getAttribute(String arg0) {
            return null;
        }

        public Enumeration getAttributeNames() {
            return null;
        }

        public String getCharacterEncoding() {
            return null;
        }

        public int getContentLength() {
            return 0;
        }

        public String getContentType() {
            return null;
        }

        public ServletInputStream getInputStream() {
            return null;
        }

        public Locale getLocale() {
            return Locale.getDefault();
        }

        public Enumeration getLocales() {
            return null;
        }

        public String getParameter(String arg0) {
            return null;
        }

        public Map getParameterMap() {
            return null;
        }

        public Enumeration getParameterNames() {
            return null;
        }

        public String[] getParameterValues(String arg0) {
            return null;
        }

        public String getProtocol() {
            return null;
        }

        public BufferedReader getReader() {
            return null;
        }

        /**
         * @deprecated
         */
        public String getRealPath(String arg0) {
            return null;
        }

        public String getRemoteAddr() {
            return null;
        }

        public String getRemoteHost() {
            return null;
        }

        public RequestDispatcher getRequestDispatcher(String arg0) {
            return null;
        }

        public String getScheme() {
            return null;
        }

        public String getServerName() {
            return null;
        }

        public int getServerPort() {
            return 0;
        }

        public boolean isSecure() {
            return false;
        }

        public void removeAttribute(String arg0) {
        }

        public void setAttribute(String arg0, Object arg1) {
        }

        public void setCharacterEncoding(String arg0) {
        }
    }

    private static final class MockResponse
    implements 
        HttpServletResponse
    {
        private final StringWriter writer = new StringWriter();
        private final PrintWriter pwriter = new PrintWriter(writer);
        
        public void addCookie(Cookie arg0) {
        }

        public void addDateHeader(String arg0, long arg1) {
        }

        public void addHeader(String arg0, String arg1) {
        }

        public void addIntHeader(String arg0, int arg1) {
        }

        public boolean containsHeader(String arg0) {
            return false;
        }

        public String getContentType() {
            return null;
        }

        public void setCharacterEncoding(String enc) {}

        /**
         * @deprecated
         */
        public String encodeRedirectUrl(String arg0) {
            return null;
        }

        public String encodeRedirectURL(String arg0) {
            return null;
        }

        /**
         * @deprecated
         */
        public String encodeUrl(String arg0) {
            return null;
        }

        public String encodeURL(String arg0) {
            return null;
        }

        public void sendError(int arg0, String arg1) {
        }

        public void sendError(int arg0) {
        }

        public void sendRedirect(String arg0) {
        }

        public void setDateHeader(String arg0, long arg1) {
        }

        public void setHeader(String arg0, String arg1) {
        }

        public void setIntHeader(String arg0, int arg1) {
        }

        /**
         * @deprecated
         */
        public void setStatus(int arg0, String arg1) {
        }

        public void setStatus(int arg0) {
        }

        public void flushBuffer() {
        }

        public int getBufferSize() {
            return 0;
        }

        public String getCharacterEncoding() {
            return null;
        }

        public Locale getLocale() {
            return null;
        }

        public ServletOutputStream getOutputStream() {
            return null;
        }

        public PrintWriter getWriter() {
            return pwriter;
        }

        public boolean isCommitted() {
            return false;
        }

        public void reset() {
        }

        public void resetBuffer() {
        }

        public void setBufferSize(int arg0) {
        }

        public void setContentLength(int arg0) {
        }

        public void setContentType(String arg0) {
        }

        public void setLocale(Locale arg0) {
        }
        
        public String toString() {
            pwriter.flush();
            return writer.toString();
        }
    }

    private static final class MockSession implements HttpSession
    {
        public Object getAttribute(String arg0) {
            return null;
        }

        public Enumeration getAttributeNames() {
            return null;
        }

        public long getCreationTime() {
            return 0;
        }

        public String getId() {
            return null;
        }

        public long getLastAccessedTime() {
            return 0;
        }

        public int getMaxInactiveInterval() {
            return 0;
        }

        public ServletContext getServletContext() {
            return null;
        }

        /**
         * @deprecated
         */
        public HttpSessionContext getSessionContext() {
            return null;
        }

        /**
         * @deprecated
         */
        public Object getValue(String arg0) {
            return null;
        }

        /**
         * @deprecated
         */
        public String[] getValueNames() {
            return null;
        }

        public void invalidate() {
        }

        public boolean isNew() {
            return false;
        }

        /**
         * @deprecated
         */
        public void putValue(String arg0, Object arg1) {
        }

        public void removeAttribute(String arg0) {
        }

        /**
         * @deprecated
         */
        public void removeValue(String arg0) {
        }

        public void setAttribute(String arg0, Object arg1) {
        }

        public void setMaxInactiveInterval(int arg0) {
        }
}
    
    /** Bootstrap for the self-test code.
     */
    public static void main( String[] argc ) throws Exception {
        TestCase test = new TestJspTaglibs( "test-jsptaglibs.txt" );
        test.run();
    }
}
