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

package freemarker.template.utility;

import java.util.List;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import freemarker.template.utility.ClassUtil;

/**
 * An object that you can make available in a template
 * to instantiate arbitrary beans-wrapped objects in a template.
 * Beware of this class's security implications. It allows
 * the instantiation of arbitrary objects and invoking 
 * methods on them. Usage is something like:
 * <br>
 * <br>myDataModel.put("objectConstructor", new ObjectConstructor());
 * <br>
 * <br>And then from your FTL code:
 * <br>
 * <br>&lt;#assign aList = objectConstructor("java.util.ArrayList", 100)&gt;
 */

public class ObjectConstructor implements TemplateMethodModelEx
{
    public Object exec(List args) throws TemplateModelException {
        if (args.isEmpty()) {
            throw new TemplateModelException("This method must have at least one argument, the name of the class to instantiate.");
        }
        String classname = args.get(0).toString();
        Class cl = null;
        try {
            cl = ClassUtil.forName(classname);
        }
        catch (Exception e) {
            throw new TemplateModelException(e.getMessage());
        }
        BeansWrapper bw = BeansWrapper.getDefaultInstance();
        Object obj = bw.newInstance(cl, args.subList(1, args.size()));
        return bw.wrap(obj);
    }
}
