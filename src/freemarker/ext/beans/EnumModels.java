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

import java.util.LinkedHashMap;
import java.util.Map;

import freemarker.template.TemplateModel;

/**
 * @author Attila Szegedi
 * @version $Id: EnumModels.java,v 1.1 2005/11/03 08:49:19 szegedia Exp $
 */
class EnumModels extends ClassBasedModelFactory {

    EnumModels(BeansWrapper wrapper) {
        super(wrapper);
    }
    
    @Override
    protected TemplateModel createModel(Class clazz) {
        Object[] obj = clazz.getEnumConstants();
        if(obj == null) {
            // Return null - it'll manifest itself as undefined in the template.
            // We're doing this rather than throw an exception as this way 
            // people can use someEnumModel?default({}) to gracefully fall back 
            // to an empty hash if they want to.
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < obj.length; i++) {
            Enum value = (Enum) obj[i];
            map.put(value.name(), value);
        }
        return new SimpleMapModel(map, getWrapper());
    }
}
