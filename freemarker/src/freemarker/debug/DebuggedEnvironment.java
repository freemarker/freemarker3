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

package freemarker.debug;

import java.rmi.RemoteException;

/**
 * Represents the debugger-side mirror of a debugged 
 * {@link freemarker.core.Environment} object in the remote VM. This interface
 * extends {@link DebugModel}, and the properties of the Environment are exposed
 * as hash keys on it. Specifically, the following keys are supported:
 * "currentNamespace", "dataModel", "globalNamespace", "knownVariables", 
 * "mainNamespace", and "template".
 * <p>The debug model for the template supports keys "configuration" and "name".
 * <p>The debug model for the configuration supports key "sharedVariables".
 * <p>Additionally, all of the debug models for environment, template, and 
 * configuration also support all the setting keys of 
 * {@link freemarker.core.Configurable} objects. 

 * @author Attila Szegedi
 * @version $Id: DebuggedEnvironment.java,v 1.1 2003/05/02 15:55:48 szegedia Exp $
 */
public interface DebuggedEnvironment extends DebugModel
{
    /**
     * Resumes the processing of the environment in the remote VM after it was 
     * stopped on a breakpoint.
     * @throws RemoteException
     */
    public void resume() throws RemoteException;
    
    /**
     * Stops the processing of the environment after it was stopped on
     * a breakpoint. Causes a {@link freemarker.core.StopException} to be
     * thrown in the processing thread in the remote VM. 
     * @throws RemoteException
     */
    public void stop() throws RemoteException;
    
    /**
     * Returns a unique identifier for this environment
     * @throws RemoteException
     */
    public long getId() throws RemoteException;
}
