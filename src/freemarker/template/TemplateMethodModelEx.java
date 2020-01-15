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

import java.util.List;

import freemarker.core.Environment;
import freemarker.template.utility.DeepUnwrap;

/**
 * A subinterface of {@link TemplateMethodModel} that acts on models, rather
 * than on strings. {@link TemplateMethodModel} interface will receive string
 * representations of its argument expressions, while this interface receives
 * the models themselves. The interface has no new methods. Instead, by 
 * implementing this interface the class declares that it wishes to receive 
 * actual TemplateModel instances in its arguments list when invoked instead of
 * their string representations. Further, if the implementation wishes to 
 * operate on POJOs that might be underlying the models, it can use the static 
 * utility methods in the {@link DeepUnwrap} class to easily obtain them.
 * @author Attila Szegedi, szegedia at users dot sourceforge dot net
 * @version $Id: TemplateMethodModelEx.java,v 1.8 2003/01/12 23:40:21 revusky Exp $
 */
public interface TemplateMethodModelEx extends TemplateMethodModel {

    /**
     * Executes a method call. 
     * @param arguments a <tt>List</tt> of {@link TemplateModel} objects
     * containing the values of the arguments passed to the method. If the 
     * implementation wishes to operate on POJOs that might be underlying the 
     * models, it can use the static utility methods in the {@link DeepUnwrap} 
     * class to easily obtain them.
     * @return the return value of the method, or null. If the returned value
     * does not implement {@link TemplateModel}, it will be automatically 
     * wrapped using the {@link Environment#getObjectWrapper() environment 
     * object wrapper}.
     */
    public Object exec(List arguments) throws TemplateModelException;
}