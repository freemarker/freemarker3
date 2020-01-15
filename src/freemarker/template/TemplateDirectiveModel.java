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
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.utility.DeepUnwrap;

/**
 * Objects that implement this interface can be used as user-defined directives 
 * (much like macros). They can do arbitrary actions, write arbitrary
 * text to the template output, and trigger rendering of their nested content
 * any number of times.
 *
 * @since 2.3.11
 * @author Attila Szegedi
 * @version $Id: $
 */
public interface TemplateDirectiveModel extends TemplateModel
{
    /**
     * Executes this user-defined directive; called by FreeMarker when the user-defined
     * directive is called in the template.
     *
     * @param env the current processing environment. Note that you can access
     * the output {@link java.io.Writer Writer} by {@link Environment#getOut()}.
     * @param params the parameters (if any) passed to the directive as a 
     * map of key/value pairs where the keys are {@link String}-s and the 
     * values are {@link TemplateModel} instances. This is never 
     * <code>null</code>. If you need to convert the template models to POJOs,
     * you can use the utility methods in the {@link DeepUnwrap} class.
     * @param loopVars an array that corresponds to the "loop variables", in
     * the order as they appear in the directive call. ("Loop variables" are out-parameters
     * that are available to the nested body of the directive; see in the Manual.)
     * You set the loop variables by writing this array. The length of the array gives the
     * number of loop-variables that the caller has specified.
     * Never <code>null</code>, but can be a zero-length array.
     * @param body an object that can be used to render the nested content (body) of
     * the directive call. If the directive call has no nested content (i.e., it is
     * [@myDirective /] or [@myDirective][/@myDirective]), then this will be
     * <code>null</code>.
     *
     * @throws TemplateException
     * @throws IOException
     */
    public void execute(Environment env, Map<String, TemplateModel> params, 
            TemplateModel[] loopVars, TemplateDirectiveBody body) 
    throws TemplateException, IOException;
}