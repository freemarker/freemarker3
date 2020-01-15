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

package freemarker.template;

import java.io.IOException;
import java.io.Writer;

/**
 * Represents the body of a directive invocation. An implementation of this 
 * class is passed to the {@link TemplateDirectiveModel#execute(freemarker.core.Environment, 
 * java.util.Map, TemplateModel[], TemplateDirectiveBody)}. The implementation of the method is
 * free to invoke it any number of times, with any writer.
 *
 * @since 2.3.11
 * @author Attila Szegedi
 * @version $Id: $
 */
public interface TemplateDirectiveBody
{
    /**
     * Renders the body of the directive body to the specified writer. The 
     * writer is not flushed after the rendering. If you pass the environment's
     * writer, there is no need to flush it. If you supply your own writer, you
     * are responsible to flush/close it when you're done with using it (which
     * might be after multiple renderings).
     * @param out the writer to write the output to.
     */
    public void render(Writer out) throws TemplateException, IOException;
}
