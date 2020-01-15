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

package freemarker.ext.script;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.Permission;
import java.util.Properties;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import freemarker.core.FreeMarkerPermission;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Implementation of a {@link ScriptEngine} for FreeMarker templates. Supports
 * compilation by implementing the {@link Compilable} interface for efficient
 * multiple execution of templates without reparsing. You might want to cast a 
 * generic script engine reference to this class if you want to invoke 
 * {@link #setConfiguration(Configuration)} to modify its configuration.
 * @author Attila Szegedi
 * @version $Id: $
 */
public class FreeMarkerScriptEngine
extends 
    AbstractScriptEngine
implements
    Compilable
{
    private static final Permission SET_CONFIGURATION = 
        new FreeMarkerPermission("setScriptEngineFactoryConfiguration");
    
    private final FreeMarkerScriptEngineFactory factory;
    private Configuration config;
    
    FreeMarkerScriptEngine(FreeMarkerScriptEngineFactory factory, 
            Configuration config)
    {
        this.factory = factory;
        this.config = config;
    }

    /**
     * Sets a new FreeMarker configuration to use by all templates created
     * by this script engine factory. By default it inherits the configuration
     * of its factory at the time this engine instance was created (if the
     * configuration of the factory is later changed, it doesn't affect this
     * engine). Compiled scripts inherit the configuration of the engine at the
     * time of invocation of the compile() method. If a security manager is set
     * in the JVM, and the invoking code doesn't posess the 
     * "freeMarker.script.setEngineConfiguration" runtime permission, a 
     * security exception is thrown. Note that you can further override this 
     * Configuration by binding an instance of Configuration or Properties into
     * the engine's Bindings under the name 
     * {@link FreeMarkerScriptConstants#CONFIGURATION}, but that is also 
     * subject to the same security check and if there are not sufficient 
     * privileges, an eval() or compile() call will also throw a 
     * SecurityException.
     * @param config the new FreeMarker configuration object
     * @throws IllegalArgumentException if config is null
     * @throws SecurityException if a security manager is set in the JVM, and 
     * the invoking code doesn't posess the 
     * "freeMarker.script.setEngineConfiguration" runtime permission.
     */
    public void setConfiguration(Configuration config)
    {
        if(config == null)
        {
            throw new IllegalArgumentException("config == null");
        }
        checkSetConfiguration();
        synchronized(this)
        {
            this.config = config;
        }
    }

    private void checkSetConfiguration()
    {
        FreeMarkerPermission.checkPermission(SET_CONFIGURATION);
    }
    
    public Bindings createBindings()
    {
        return new SimpleBindings();
    }
    
    public Object eval(String script, ScriptContext context)
    throws ScriptException
    {
        return eval(new StringReader(script), context);
    }

    public Object eval(Reader script, ScriptContext context)
    throws ScriptException
    {
        return compile(script).eval(context);
    }

    public ScriptEngineFactory getFactory()
    {
        return factory;
    }

    public CompiledScript compile(String script) throws ScriptException
    {
        return compile(new StringReader(script));
    }

    public CompiledScript compile(Reader script) throws ScriptException
    {
        String name = (String)get(ScriptEngine.FILENAME);
        if(name == null)
        {
            name = "<unknown source>";
        }
        try
        {
            Configuration config;
            Object objConfig = get(FreeMarkerScriptConstants.CONFIGURATION);
            if(objConfig != null)
            {
                if(objConfig instanceof Configuration)
                {
                    synchronized(this)
                    {
                        if(objConfig != this.config)
                        {
                            checkSetConfiguration();
                        }
                    }
                    config = (Configuration)objConfig;
                }
                else if(objConfig instanceof Properties)
                {
                    checkSetConfiguration();
                    config = new Configuration();
                    config.setSettings((Properties)objConfig);
                }
                else
                {
                    throw new ScriptException("Engine property " + 
                            FreeMarkerScriptConstants.CONFIGURATION + " is " +
                            "neither a Configuration nor Properties, it is " +
                            objConfig.getClass().getName());
                }
            }
            else
            {
                synchronized(this)
                {
                    config = this.config;
                }
            }
            return new CompiledFreeMarkerScript(this, new Template(name, 
                    script, config));
        }
        catch(IOException e)
        {
            throw new ScriptException(e);
        }
        catch(TemplateException e)
        {
            throw new ScriptException(e);
        }
    }
}