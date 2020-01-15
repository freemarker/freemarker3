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

package freemarker.core;

import java.util.Collection;

import freemarker.template.*;

/**
 * Represents a variable resolution context in FTL. This 
 * may be the local variables in a macro, the context of a loop
 * or a template namespace 
 * @author Jonathan Revusky
 */

public interface Scope extends TemplateHashModelEx {

    /**
     * Set a variable in this scope. This 
     * will typically only be used internally by the FreeMarker engine.
     */
    void put(String key, TemplateModel value);

    /**
     * Removes a variable in this scope.
     * This will typically only be used by FreeMarker engine internals 
     */
    TemplateModel remove(String key);

    /**
     * @return the Environment object associated with this Scope.
     */

    Environment getEnvironment();

    /**
     * @return the Template object associated with this Scope.
     */
    Template getTemplate();

    /**
     * @return whether the variable is defined in
     * this specific scope. (It could be defined in a 
     * fallback scope and this method will return false.)
     */
    boolean definesVariable(String name);

    /**
     * @return the fallback Scope for variable resolution
     */
    Scope getEnclosingScope();


    /**
     * Evaluates the variable of this name in this scope,
     * falling back to the enclosing Scope if it is not
     * defined in this one.
     */
    TemplateModel resolveVariable(String name) throws TemplateModelException;

    /**
     * Returns the names of variables directly managed by this scope (i.e. it 
     * does not traverse the chain of enclosing scopes, but limits itself to 
     * this scope only).
     * @return a collection of known variable names for this scope, without
     * enclosing scopes. The returned collection should be either immutable, or
     * it should be disconnected from the scope, so any modifications to the
     * collection don't affect the scope.
     * @throws TemplateModelException
     */
    Collection<String> getDirectVariableNames() throws TemplateModelException;
    
    
}
