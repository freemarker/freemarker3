package freemarker.core;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import freemarker.core.ast.Macro;
import freemarker.core.ast.TemplateElement;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Abstract base class for {@link Template}. Used to provide package-level 
 * access to secured internals used by {@link Environment}.
 * @author Attila Szegedi
 * @version $Id: $
 */
public abstract class TemplateCore extends Configurable
{
    private TemplateElement rootElement;
    private Map<String, Macro> macros = new HashMap<String, Macro>();
    
    protected TemplateCore(Configuration config) {
        super(config);
    }
    
    /**
     * Called by code internally to maintain
     * a table of macros
     */
    public void addMacro(Macro macro) {
        String macroName = macro.getName();
        synchronized(macros) {
            macros.put(macroName, macro);
        }
    }

    public Map<String,Macro> getMacros() {
        return macros;
    }
    
    protected synchronized TemplateElement getRootElement() {
        return rootElement;
    }
    
    protected synchronized void setRootElement(TemplateElement rootElement) {
        if(this.rootElement != null) {
            throw new IllegalStateException("Root element already set");
        }
        this.rootElement = rootElement;
    }
}
