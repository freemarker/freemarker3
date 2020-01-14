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

package freemarker.ext.beans;

import java.lang.ref.WeakReference;

import org.zeroturnaround.javarebel.ClassEventListener;
import org.zeroturnaround.javarebel.ReloaderFactory;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class JavaRebelIntegration
{
    static void testAvailability() {
        ReloaderFactory.getInstance();
    }
    
    /**
     * Adds a JavaRebel class reloading listener for a that will invalidate 
     * cached information for that class in the specified BeansWrapper when the
     * class is reloaded. The beans wrapper is weakly referenced and the 
     * listener is unregistered if the wrapper is garbage collected.
     * @param w the beans wrapper to register.
     */
    static void registerWrapper(BeansWrapper w) {
        ReloaderFactory.getInstance().addClassReloadListener(
                new BeansWrapperCacheInvalidator(w));
    }
    
    private static class BeansWrapperCacheInvalidator 
    implements ClassEventListener
    {
        private final WeakReference ref;
        
        BeansWrapperCacheInvalidator(BeansWrapper w) {
            ref = new WeakReference(w);
        }
        
        public void onClassEvent(int eventType, Class klass) {
            BeansWrapper wrapper = (BeansWrapper)ref.get();
            if(wrapper == null) {
                ReloaderFactory.getInstance().removeClassReloadListener(this);
            }
            else if(eventType == ClassEventListener.EVENT_RELOADED) {
                wrapper.removeIntrospectionInfo(klass);
            }
        }
    }
}
