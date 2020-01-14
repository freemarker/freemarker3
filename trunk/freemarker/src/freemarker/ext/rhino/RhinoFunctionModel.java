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

import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * @author Attila Szegedi
 * @version $Id: RhinoFunctionModel.java,v 1.2 2005/06/22 10:52:52 ddekany Exp $
 */
public class RhinoFunctionModel extends RhinoScriptableModel 
implements TemplateMethodModelEx {

    private final Scriptable fnThis;
    
    public RhinoFunctionModel(Function function, Scriptable fnThis, 
            BeansWrapper wrapper) {
        super(function, wrapper);
        this.fnThis = fnThis;
    }
    
    public Object exec(List arguments) throws TemplateModelException {
        Context cx = Context.getCurrentContext();
        Object[] args = arguments.toArray();
        BeansWrapper wrapper = getWrapper();
        for (int i = 0; i < args.length; i++) {
            args[i] = unwrap(args[i], wrapper);
        }
        return wrapper.wrap(((Function)getScriptable()).call(cx, 
                ScriptableObject.getTopLevelScope(fnThis), fnThis, args));
    }

    private Object unwrap(Object arg, BeansWrapper wrapper)
            throws TemplateModelException
    {
        Object obj = wrapper.unwrap((TemplateModel)arg, Scriptable.class);
        if(obj == BeansWrapper.CAN_NOT_UNWRAP) {
            throw new TemplateModelException("Can not convert argument to Rhino Scriptable");
        }
        return obj;
    }
}
