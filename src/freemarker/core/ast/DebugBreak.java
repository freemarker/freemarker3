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

package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.core.StopException;
import freemarker.debug.impl.DebuggerService;
import freemarker.template.TemplateException;

/**
 * @author Attila Szegedi
 * @version $Id: DebugBreak.java,v 1.2 2004/09/11 13:11:14 stephanmueller Exp $
 */
public class DebugBreak extends TemplateElement
{
    public DebugBreak(TemplateElement nestedBlock)
    {
        this.nestedBlock = nestedBlock;
        nestedBlock.setParent(this);
        copyLocationFrom(nestedBlock);
    }
    
    public void execute(Environment env) throws TemplateException, IOException
    {
        if(!DebuggerService.suspendEnvironment(env, nestedBlock.getBeginLine()))
        {
            nestedBlock.execute(env);
        }
        else
        {
            throw new StopException(env, "Stopped by debugger");        
        }
    }

    public String getDescription()
    {
        return nestedBlock.getDescription();
    }
}
