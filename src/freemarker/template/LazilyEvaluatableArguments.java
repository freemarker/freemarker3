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

/**
 * A marker interface that tells the FreeMarker that the method arguments can
 * be lazily evaluated. Method models implementing this interface declare that
 * their arguments needn't be evaluated before the method is invoked, but that
 * each argument should be evaluated only when it is first retrieved from the
 * argument list by the model. While this interface extends 
 * {@link TemplateMethodModel} to reinforce the notion that it is to be applied
 * to method models, it can naturally be implemented by classes that implement
 * {@link TemplateMethodModelEx}. 
 * Note that for the time being, there is a limitation in the FreeMarker 
 * implementation that prevents lazy evaluation of arguments of method models
 * that have a {@link Parameters} annotation on them. If your class 
 * implementing {@link LazilyEvaluatableArguments} also has a 
 * {@link Parameters} annotation, its arguments will be eagerly evaluated. This
 * limitation may be lifted in a future version of FreeMarker. 
 * @author Attila Szegedi
 * @version $Id: LazilyEvaluatableArguments.java,v 1.2 2005/11/03 08:45:08 szegedia Exp $
 */
public interface LazilyEvaluatableArguments extends TemplateMethodModel {
}
