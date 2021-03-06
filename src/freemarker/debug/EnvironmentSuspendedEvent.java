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

import java.util.EventObject;

/**
 * Event describing a suspension of an environment (ie because it hit a
 * breakpoint).
 * @author Attila Szegedi
 * @version $Id: EnvironmentSuspendedEvent.java,v 1.1 2003/05/02 15:55:48 szegedia Exp $
 */
public class EnvironmentSuspendedEvent extends EventObject
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -999730114010871063L;
	private final int line;
    private final DebuggedEnvironment env;

    public EnvironmentSuspendedEvent(Object source, int line, DebuggedEnvironment env)
    {
        super(source);
        this.line = line;
        this.env = env;
    }

    /**
     * The line number in the template where the execution of the environment
     * was suspended.
     * @return int the line number
     */
    public int getLine()
    {
        return line;
    }

    /**
     * The environment that was suspended
     * @return DebuggedEnvironment
     */
    public DebuggedEnvironment getEnvironment()
    {
        return env;
    }
}
