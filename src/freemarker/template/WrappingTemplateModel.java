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
 * A base class for containers that wrap arbitrary Java objects into 
 * {@link TemplateModel} instances.
 *
 * @version $Id: WrappingTemplateModel.java,v 1.19 2006/03/15 17:46:23 revusky Exp $
 */
abstract public class WrappingTemplateModel {

    private static ObjectWrapper defaultObjectWrapper = 
        DefaultObjectWrapper.instance;
    private ObjectWrapper objectWrapper;
    
    /**
     * Sets the default object wrapper that is used when a wrapping template
     * model is constructed without being passed an explicit object wrapper.
     * The default value is {@link ObjectWrapper#SIMPLE_WRAPPER}.
     * Note that {@link Configuration#setSharedVariable(String, Object)} and
     * {@link Template#process(Object, java.io.Writer)} don't use this setting,
     * they rather use whatever object wrapper their 
     * {@link Configuration#getObjectWrapper()} method returns.
     */
    public static void setDefaultObjectWrapper(ObjectWrapper objectWrapper) {
        defaultObjectWrapper = objectWrapper;
    }

    /**
     * Returns the default object wrapper that is used when a wrapping template
     * model is constructed without being passed an explicit object wrapper.
     * Note that {@link Configuration#setSharedVariable(String, Object)} and
     * {@link Template#process(Object, java.io.Writer)} don't use this setting,
     * they rather use whatever object wrapper their 
     * {@link Configuration#getObjectWrapper()} method returns.
     */
    public static ObjectWrapper getDefaultObjectWrapper() {
        return defaultObjectWrapper;
    }
    
    /**
     * Protected constructor that creates a new wrapping template model using
     * the default object wrapper.
     */
    protected WrappingTemplateModel() {
        this(defaultObjectWrapper);
    }

    /**
     * Protected constructor that creates a new wrapping template model using
     * the specified object wrapper.
     * @param objectWrapper the wrapper to use. If null is passed, the default
     * object wrapper is used.
     */
    protected WrappingTemplateModel(ObjectWrapper objectWrapper) {
        this.objectWrapper = 
            objectWrapper != null ? objectWrapper : defaultObjectWrapper;
        if (this.objectWrapper == null) { // This is to address some weird static initializing bug
            this.objectWrapper = defaultObjectWrapper =  new DefaultObjectWrapper();
        }
    }
    
    /**
     * Returns the object wrapper instance used by this wrapping template model.
     */
    public ObjectWrapper getObjectWrapper() {
        return objectWrapper;
    }

    public void setObjectWrapper(ObjectWrapper objectWrapper) {
        this.objectWrapper = objectWrapper;
    }

    /**
     * Wraps the passed object into a template model using this object's object
     * wrapper, except that null is not wrapped but simply returned.
     * @param obj the object to wrap
     * @return the template model that wraps the object
     * @throws TemplateModelException if the wrapper does not know how to
     * wrap the passed object.
     */
    protected final TemplateModel wrap(Object obj) throws TemplateModelException {
//    	if (obj == null) return null;
        return objectWrapper.wrap(obj);
    }
}
