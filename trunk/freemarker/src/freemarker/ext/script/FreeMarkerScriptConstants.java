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

import javax.script.ScriptContext;

/**
 * Various variable names that you can use in various bindings to customize the
 * operation of the scripts.
 * @author Attila Szegedi
 * @version $Id: $
 */
public class FreeMarkerScriptConstants
{
    /**
     * If you bind an instance of Boolean.TRUE under this name in your script
     * context, the eval() will return the output of the template as a string.
     * When this value is not present (or it doesn't equal Boolean.TRUE), 
     * eval() calls return null, and the template output goes to 
     * {@link ScriptContext#getWriter()}.
     */
    public static final String STRING_OUTPUT = "freeMarker.stringOutput";
    
    /**
     * If you bind an instance of Configuration under this name in your script
     * engine, it will be used when evaluting a script, compiling a script, and
     * evaluating a script compiled from that engine. However, if there is a 
     * security manager in the JVM and the invoking code does not posess the 
     * "freeMarker.script.setEngineConfiguration" runtime permission, the 
     * attempt to evaluate/compile a script will throw a SecurityException.
     * Instead of a Configuration, you can also bind an instance of Properties.
     * In this case, a new Configuration object will be constructed on each
     * evaluate/compile operation and initialized from the Properties object.
     * This is however not really recommended, as it creates a new 
     * Configuration for each template - it is also subject to the security
     * check, of course.
     */
    public static final String CONFIGURATION = "freeMarker.configuration";
}
