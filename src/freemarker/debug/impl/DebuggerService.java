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

package freemarker.debug.impl;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import freemarker.core.Environment;
import freemarker.template.Template;
import freemarker.template.utility.SecurityUtilities;

/**
 * This class provides debugging hooks for the core FreeMarker engine. It is
 * not usable for anyone outside the FreeMarker core classes. It is public only
 * as an implementation detail.
 * @author Attila Szegedi
 * @version $Id: DebuggerService.java,v 1.2 2003/05/30 16:51:53 szegedia Exp $
 */
public abstract class DebuggerService
{
    private static final DebuggerService instance = createInstance();
    
    private static DebuggerService createInstance()
    {
        // Creates the appropriate service class. If the debugging is turned
        // off, this is a fast no-op service, otherwise it is the real-thing
        // RMI service.
        return 
            SecurityUtilities.getSystemProperty("freemarker.debug.password") == null
            ? (DebuggerService)new NoOpDebuggerService()
            : (DebuggerService)new RmiDebuggerService();
    }

    public static List getBreakpoints(String templateName)
    {
        return instance.getBreakpointsSpi(templateName);
    }
    
    abstract List getBreakpointsSpi(String templateName);

    public static void registerTemplate(Template template)
    {
        instance.registerTemplateSpi(template);
    }
    
    abstract void registerTemplateSpi(Template template);
    
    public static boolean suspendEnvironment(Environment env, int line)
    throws
        RemoteException
    {
        return instance.suspendEnvironmentSpi(env, line);
    }
    
    abstract boolean suspendEnvironmentSpi(Environment env, int line)
    throws
        RemoteException;

    private static class NoOpDebuggerService extends DebuggerService
    {
        List getBreakpointsSpi(String templateName)
        {
            return Collections.EMPTY_LIST;
        }
        
        boolean suspendEnvironmentSpi(Environment env, int line)
        {
            throw new UnsupportedOperationException();
        }
        
        void registerTemplateSpi(Template template)
        {
        }
    }
}