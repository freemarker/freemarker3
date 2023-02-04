package freemarker.core.ast;

import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.core.parser.ParseException;

/**
 * A reference to a built-in identifier, such as .root
 */
public class BuiltinVariable extends Expression {

    static final String TEMPLATE_NAME = "template_name";
    static final String NAMESPACE = "namespace";
    static final String MAIN = "main";
    static final String GLOBALS = "globals";
    static final String LOCALS = "locals";
    static final String DATA_MODEL = "data_model";
    static final String LANG = "lang";
    static final String LOCALE = "locale";
    static final String CURRENT_NODE = "current_node";
    static final String NODE = "node";
    static final String PASS = "pass";
    static final String VARS = "vars";
    static final String VERSION = "version";
    static final String ERROR = "error";
    static final String OUTPUT_ENCODING = "output_encoding";
    static final String SCOPE = "scope";
    static final String URL_ESCAPING_CHARSET = "url_escaping_charset";

    private String name;

    public BuiltinVariable(String name) throws ParseException {
        name = name.intern();
        this.name = name;
        if (name != NAMESPACE
            && name != TEMPLATE_NAME
            && name != MAIN
            && name != GLOBALS
            && name != LOCALS
            && name != LANG
            && name != LOCALE
            && name != DATA_MODEL
            && name != CURRENT_NODE
            && name != NODE
            && name != PASS
            && name != VARS
            && name != SCOPE
	    && name != VERSION
	    && name != OUTPUT_ENCODING
	    && name != URL_ESCAPING_CHARSET
            && name != ERROR)
        {
            throw new ParseException("Unknown built-in variable: " + name, this);
        }
    }
    
    public String getName() {
    	return name;
    }

    TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
        if (name == NAMESPACE) {
            return env.getCurrentNamespace();
        }
        if (name == MAIN) {
            return env.getMainNamespace();
        }
        if (name == GLOBALS) {
        	return env;
//        	return new GlobalsHash(env);
        }
        if (name == LOCALS) {
            return env.getCurrentMacroContext();
        }
        if (name == DATA_MODEL) {
            return env.getDataModel();
        }
        if (name==SCOPE) {
        	return env.getCurrentScope();
        }
        if (name == VARS) {
            return new VarsHash(env);
        }
        if (name == LOCALE) {
            return new SimpleScalar(env.getLocale().toString());
        }
        if (name == LANG) {
            return new SimpleScalar(env.getLocale().getLanguage());
        }
        if (name == CURRENT_NODE || name == NODE) {
            return env.getCurrentVisitorNode();
        }
        if (name == PASS) {
            return Macro.DO_NOTHING_MACRO;
        }
        if (name == TEMPLATE_NAME) {
            return new SimpleScalar(env.getTemplate().getName());
        }
        if (name == VERSION) {
            return new SimpleScalar(Configuration.getVersionNumber());
        }
        if (name == OUTPUT_ENCODING) {
            String s = env.getOutputEncoding();
            return s != null ? new SimpleScalar(s) : null;
        }
        if (name == URL_ESCAPING_CHARSET) {
            String s = env.getURLEscapingCharset();
            return s != null ? new SimpleScalar(s) : null;
        }
        if (name == ERROR) {
            return new SimpleScalar(env.getCurrentRecoveredErrorMessage());
        }
        throw new TemplateException("Invalid built-in variable: " + this, env);
    }

    public String toString() {
        return "." + name;
    }

    boolean isLiteral() {
        return false;
    }

    Expression _deepClone(String name, Expression subst) {
        return this;
    }

    static class VarsHash implements TemplateHashModel {
        
        private final Environment env;
        
        VarsHash(Environment env) {
            this.env = env;
        }
        
        public TemplateModel get(String key) throws TemplateModelException {
            return env.getVariable(key);
        }
        
        public boolean isEmpty() {
            return false;
        }
    }
}
