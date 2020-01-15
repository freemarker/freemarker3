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

import freemarker.ext.beans.BeansWrapper;

/**
 * <p>An object that knows how to "wrap" a java object
 * as a TemplateModel instance.
 *
 * @version $Id: ObjectWrapper.java,v 1.15 2003/06/22 17:50:28 ddekany Exp $
 */
public interface ObjectWrapper {
    /**
     * An ObjectWrapper that works similarly to {@link #SIMPLE_WRAPPER}, but
     * exposes the objects methods and JavaBeans properties as hash elements
     * and custom handling for Java Maps, ResourceBundles, etc.
     */
    ObjectWrapper BEANS_WRAPPER = BeansWrapper.getDefaultInstance();

    /**
     * The default object wrapper implementation.
     * Wraps Maps as SimpleHash and Lists as SimpleSequences, Strings and 
     * Numbers as SimpleScalar and SimpleNumber respectively.
     * Other objects are beans-wrapped, thus exposing reflection-based information.
     */
    ObjectWrapper DEFAULT_WRAPPER = DefaultObjectWrapper.instance;

    /**
     * Object wrapper that uses SimpleXXX wrappers only.
     * This wrapper has far more restrictive semantics. It 
     * behaves like the DEFAULT_WRAPPER, but for objects
     * that it does not know how to wrap as a SimpleXXX, it 
     * throws an exception. It makes no use of reflection-based 
     * exposure of methods. 
     */
    ObjectWrapper SIMPLE_WRAPPER = SimpleObjectWrapper.instance;
    
    /**
     * @return a TemplateModel wrapper of the object passed in.
     */
    TemplateModel wrap(Object obj) throws TemplateModelException;
}
