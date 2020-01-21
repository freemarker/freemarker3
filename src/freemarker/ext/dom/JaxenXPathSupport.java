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
 
package freemarker.ext.dom;

import freemarker.template.*;
import freemarker.template.utility.UndeclaredThrowableException;
import freemarker.cache.TemplateCache;
import freemarker.core.CustomAttribute;
import freemarker.core.Environment;

import org.jaxen.*;
import org.jaxen.dom.DocumentNavigator;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * @author Jonathan Revusky
 * @version $Id: JaxenXPathSupport.java,v 1.30 2003/12/20 01:17:30 ddekany Exp $
 */
class JaxenXPathSupport implements XPathSupport {
    
    private static final CustomAttribute cache = 
        new CustomAttribute(CustomAttribute.SCOPE_TEMPLATE) {
            protected Object create() {
                return new HashMap();
            }
        };

    private final static ArrayList EMPTY_ARRAYLIST = new ArrayList();

    public TemplateModel executeQuery(Object context, String xpathQuery) throws TemplateModelException {
        try {
            BaseXPath xpath;
            Map xpathCache = (Map)cache.get();
            synchronized(xpathCache) {
                xpath = (BaseXPath) xpathCache.get(xpathQuery);
                if (xpath == null) {
                    xpath = new BaseXPath(xpathQuery, fmDomNavigator);
                    xpath.setNamespaceContext(customNamespaceContext);
                    xpath.setFunctionContext(fmFunctionContext);
                    xpath.setVariableContext(fmVariableContext);
                    xpathCache.put(xpathQuery, xpath);
                }
            }
            List result = xpath.selectNodes(context != null ? context : EMPTY_ARRAYLIST);
            if (result.size() == 1) {
                return ObjectWrapper.DEFAULT_WRAPPER.wrap(result.get(0));
            }
            NodeListModel nlm = new NodeListModel(result, null);
            nlm.xpathSupport = this;
            return nlm;
        } catch (UndeclaredThrowableException e) {
            Throwable t  = e.getUndeclaredThrowable();
            if(t instanceof TemplateModelException) {
                throw (TemplateModelException)t;
            }
            throw e;
        } catch (JaxenException je) {
            throw new TemplateModelException(je);
        }
    }

    static private final NamespaceContext customNamespaceContext = new NamespaceContext() {
        
        public String translateNamespacePrefixToUri(String prefix) {
            if (prefix.equals(Template.DEFAULT_NAMESPACE_PREFIX)) {
                return Environment.getCurrentEnvironment().getDefaultNS();
            }
            return Environment.getCurrentEnvironment().getNamespaceForPrefix(prefix);
        }
    };

    private static final VariableContext fmVariableContext = new VariableContext() {
        public Object getVariableValue(String namespaceURI, String prefix, String localName)
        throws 
            UnresolvableException
        {
            try {
                TemplateModel model = Environment.getCurrentEnvironment().getVariable(localName);
                if(model == null) {
                    throw new UnresolvableException("Variable " + localName + " not found.");
                }
                if(model instanceof TemplateScalarModel) {
                    return ((TemplateScalarModel)model).getAsString();
                }
                if(model instanceof TemplateNumberModel) {
                    return ((TemplateNumberModel)model).getAsNumber();
                }
                if(model instanceof TemplateDateModel) {
                    return ((TemplateDateModel)model).getAsDate();
                }
                if(model instanceof TemplateBooleanModel) {
                    return ((TemplateBooleanModel)model).getAsBoolean() ? Boolean.TRUE : Boolean.FALSE;
                }
            }
            catch(TemplateModelException e) {
                throw new UndeclaredThrowableException(e);
            }
            throw new UnresolvableException("Variable " + localName + " is not a string, number, date, or boolean");
        }
    };
     
    private static final FunctionContext fmFunctionContext = new XPathFunctionContext() {
        public Function getFunction(String namespaceURI, String prefix, String localName)
        throws UnresolvableException {
            try {
                return super.getFunction(namespaceURI, prefix, localName);
            } 
            catch(UnresolvableException e) {
                return super.getFunction(null, null, localName);
            }
        }
    };
    
    private static final CustomAttribute cachedTree = new CustomAttribute(CustomAttribute.SCOPE_TEMPLATE);
     
    private static final Navigator fmDomNavigator = new DocumentNavigator() {
        /**
		 * 
		 */
		private static final long serialVersionUID = 6718387093084486426L;

		public Object getDocument(String uri) throws FunctionCallException
        {
            try
            {
                Template raw = getTemplate(uri);
                Document doc = (Document)cachedTree.get(raw);
                if(doc == null) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    FmEntityResolver er = new FmEntityResolver();
                    builder.setEntityResolver(er);
                    doc = builder.parse(createInputSource(null, raw));
                    // If the entity resolver got called 0 times, the document
                    // is standalone, so we can safely cache it
                    if(er.getCallCount() == 0) {
                        cachedTree.set(doc, raw);
                    }
                }
                return doc;
            }
            catch (Exception e)
            {
                throw new FunctionCallException("Failed to parse document for URI: " + uri, e);
            }
        }
    };

    static Template getTemplate(String systemId) throws IOException {
        Environment env = Environment.getCurrentEnvironment();
        String encoding = env.getTemplate().getEncoding();
        if (encoding == null) {
            encoding = env.getConfiguration().getEncoding(env.getLocale());
        }
        String templatePath = env.getTemplate().getName();
        int lastSlash = templatePath.lastIndexOf('/');
        templatePath = lastSlash == -1 ? "" : templatePath.substring(0, lastSlash + 1);
        systemId = TemplateCache.getFullTemplatePath(env, templatePath, systemId);
        Template raw = env.getConfiguration().getTemplate(systemId, env.getLocale(), encoding, false);
        return raw;
    }

    private static InputSource createInputSource(String publicId, Template raw) throws IOException, SAXException {
        StringWriter sw = new StringWriter();
        try {
            raw.process(Collections.EMPTY_MAP, sw);
        }
        catch(TemplateException e) {
            throw new SAXException(e);
        }
        InputSource is = new InputSource();
        is.setPublicId(publicId);
        is.setSystemId(raw.getName());
        is.setCharacterStream(new StringReader(sw.toString()));
        return is;
    }

    private static class FmEntityResolver implements EntityResolver {
        private int callCount = 0;
        
        public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException, IOException {
            ++callCount;
            return createInputSource(publicId, getTemplate(systemId));
        }
        
        int getCallCount() {
            return callCount;
        }
    };
}
