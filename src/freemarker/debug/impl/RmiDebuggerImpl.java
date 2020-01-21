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
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.List;

import freemarker.debug.Breakpoint;
import freemarker.debug.Debugger;
import freemarker.debug.DebuggerListener;

/**
 * @author Attila Szegedi
 * @version $Id: RmiDebuggerImpl.java,v 1.2 2004/09/09 15:20:37 szegedia Exp $
 */
class RmiDebuggerImpl
extends
    UnicastRemoteObject
implements
    Debugger
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 4192307465600345394L;
	private final RmiDebuggerService service;
    
    protected RmiDebuggerImpl(RmiDebuggerService service) throws RemoteException
    {
        this.service = service;
    }

    public void addBreakpoint(Breakpoint breakpoint)
    {
        service.addBreakpoint(breakpoint);
    }

    public Object addDebuggerListener(DebuggerListener listener)
    {
        return service.addDebuggerListener(listener);
    }

    public List getBreakpoints()
    {
        return service.getBreakpointsSpi();
    }

    public List getBreakpoints(String templateName)
    {
        return service.getBreakpointsSpi(templateName);
    }

    public Collection getSuspendedEnvironments()
    {
        return service.getSuspendedEnvironments();
    }

    public void removeBreakpoint(Breakpoint breakpoint)
    {
        service.removeBreakpoint(breakpoint);
    }

    public void removeDebuggerListener(Object id)
    {
        service.removeDebuggerListener(id);
    }

    public void removeBreakpoints()
    {
        service.removeBreakpoints();
    }

    public void removeBreakpoints(String templateName)
    {
        service.removeBreakpoints(templateName);
    }
}
