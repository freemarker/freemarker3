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
import java.io.StringWriter;
import java.io.Writer;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Class that represents a parsed FreeMarker {@link Template} object as a 
 * JSR-223 {@link CompiledScript}.
 * @author Attila Szegedi
 * @version $Id: $
 */
class CompiledFreeMarkerScript extends CompiledScript
{
    private final FreeMarkerScriptEngine engine;
    private final Template template;
    
    CompiledFreeMarkerScript(FreeMarkerScriptEngine engine, Template template)
    {
        this.engine = engine;
        this.template = template;
    }

    @Override
    public Object eval(ScriptContext context) throws ScriptException
    {
        try
        {
            boolean stringOutput = Boolean.TRUE.equals(context.getAttribute(
                    FreeMarkerScriptConstants.STRING_OUTPUT));
            
            Writer w = stringOutput ? new StringWriter() : context.getWriter();
            
            template.process(new ScriptContextHashModel(context, 
                    template.getObjectWrapper()), w);
            
            return stringOutput ? w.toString() : null;
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

    @Override
    public ScriptEngine getEngine()
    {
        return engine;
    }
}
