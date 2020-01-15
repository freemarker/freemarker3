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

package freemarker.ext.rhino;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;
import org.mozilla.javascript.Wrapper;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.util.ModelFactory;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * @author Attila Szegedi
 * @version $Id: RhinoWrapper.java,v 1.2 2005/06/22 10:52:52 ddekany Exp $
 */
public class RhinoWrapper extends BeansWrapper {

    public TemplateModel wrap(Object obj) throws TemplateModelException {
        // So our existence builtins work as expected.
        if(obj == Undefined.instance || obj == UniqueTag.NOT_FOUND) {
            return null;
        }
        // UniqueTag.NULL_VALUE represents intentionally set null in Rhino, and
        // TemplateModel.JAVA_NULL also represents intentionally returned null.
        // I [A.Sz.] am fairly certain that this value is never passed out of
        // any of the Rhino code back to clients, but is instead always being
        // converted back to null. However, since this object is available to 
        // any 3rd party Scriptable implementations as well, they might return
        // it, so we'll just be on the safe side, and handle it.
        if(obj == UniqueTag.NULL_VALUE) {
            return TemplateModel.JAVA_NULL;
        }
        // So, say, a JavaAdapter for FreeMarker interfaces works
        if(obj instanceof Wrapper) {
            obj = ((Wrapper)obj).unwrap();
        }
        return super.wrap(obj);
    }
    
    @Override
    protected ModelFactory getModelFactory(Class clazz) {
        if(Scriptable.class.isAssignableFrom(clazz)) {
            return RhinoScriptableModel.FACTORY;
        }
        return super.getModelFactory(clazz);
    }
}