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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

/**
 * Utility methods for unwrapping {@link TemplateModel}-s.
 * @author Attila Szegedi
 * @version $Id: DeepUnwrap.java,v 1.6 2005/06/16 18:13:58 ddekany Exp $
 */
public class DeepUnwrap
{
    private static final Class OBJECT_CLASS = Object.class;
    /**
     * Unwraps {@link TemplateModel}-s recursively.
     * The converting of the {@link TemplateModel} object happens with the following rules:
     * <ol>
     *   <li>If the object implements {@link AdapterTemplateModel}, then the result
     *       of {@link AdapterTemplateModel#getAdaptedObject(Class)} for <tt>Object.class</tt> is returned.
     *   <li>If the object implements {@link WrapperTemplateModel}, then the result
     *       of {@link WrapperTemplateModel#getWrappedObject()} is returned.
     *   <li>If the object implements {@link TemplateScalarModel}, then the result
     *       of {@link TemplateScalarModel#getAsString()} is returned.
     *   <li>If the object implements {@link TemplateNumberModel}, then the result
     *       of {@link TemplateNumberModel#getAsNumber()} is returned.
     *   <li>If the object implements {@link TemplateDateModel}, then the result
     *       of {@link TemplateDateModel#getAsDate()} is returned.
     *   <li>If the object implements {@link TemplateBooleanModel}, then the result
     *       of {@link TemplateBooleanModel#getAsBoolean()} is returned.
     *   <li>If the object implements {@link TemplateSequenceModel} or
     *       {@link TemplateCollectionModel}, then a <code>java.util.ArrayList</code> is
     *       constructed from the subvariables, and each subvariable is unwrapped with
     *       the rules described here (recursive unwrapping).
     *   <li>If the object implements {@link TemplateHashModelEx}, then a
     *       <code>java.util.HashMap</code> is constructed from the subvariables, and each
     *       subvariable is unwrapped with the rules described here (recursive unwrapping).
     *   <li>If the object is {@link TemplateModel#JAVA_NULL}, then null is returned.
     *   <li>Throw a <code>TemplateModelException</code>, because it doesn't know how to
     *       unwrap the object.
     * </ol>
     */
    public static Object unwrap(TemplateModel model) throws TemplateModelException {
        return unwrap(model, false);
    }

    /**
     * Same as {@link #unwrap(TemplateModel)}, but it doesn't throw exception 
     * if it doesn't know how to unwrap the model, but rather returns it as-is.
     * @since 2.3.14
     */
    public static Object permissiveUnwrap(TemplateModel model) throws TemplateModelException {
        return unwrap(model, true);
    }
    
    /**
     * @deprecated the name of this method is mistyped. Use 
     * {@link #permissiveUnwrap(TemplateModel)} instead.
     */
    public static Object premissiveUnwrap(TemplateModel model) throws TemplateModelException {
        return unwrap(model, true);
    }
    
    private static Object unwrap(TemplateModel model, boolean permissive) throws TemplateModelException {
        if(model instanceof AdapterTemplateModel) {
            return ((AdapterTemplateModel)model).getAdaptedObject(OBJECT_CLASS);
        }
        if (model instanceof WrapperTemplateModel) {
            return ((WrapperTemplateModel)model).getWrappedObject();
        }
        if(model instanceof TemplateScalarModel) {
            return ((TemplateScalarModel)model).getAsString();
        }
        if(model instanceof TemplateNumberModel) {
            return ((TemplateNumberModel)model).getAsNumber();
        }
        if(model instanceof TemplateDateModel) {
            return ((TemplateDateModel)model).getAsDate();
        }
        if(model instanceof TemplateBooleanModel) {
            return ((TemplateBooleanModel)model).getAsBoolean() ? Boolean.TRUE : Boolean.FALSE;
        }
        if(model instanceof TemplateSequenceModel) {
            TemplateSequenceModel seq = (TemplateSequenceModel)model;
            ArrayList<Object> list = new ArrayList<Object>(seq.size());
            for(int i = 0; i < seq.size(); ++i) {
                list.add(unwrap(seq.get(i), permissive));
            }
            return list;
        }
        if(model instanceof TemplateCollectionModel) {
            TemplateCollectionModel coll = (TemplateCollectionModel)model;
            ArrayList<Object> list = new ArrayList<Object>();
            TemplateModelIterator it = coll.iterator();            
            while(it.hasNext()) {
                list.add(unwrap(it.next(), permissive));
            }
            return list;
        }
        if(model instanceof TemplateHashModelEx) {
            TemplateHashModelEx hash = (TemplateHashModelEx)model;
            Map map = new LinkedHashMap();
            TemplateModelIterator keys = hash.keys().iterator();
            while(keys.hasNext()) {
                String key = (String)unwrap(keys.next(), permissive); 
                map.put(key, unwrap(hash.get(key), permissive));
            }
            return map;
        }
        if(model == TemplateModel.JAVA_NULL) {
            return null;
        }
        if (permissive) {
            return model;
        }
        throw new TemplateModelException("Cannot deep-unwrap model of type " + model.getClass().getName());
    }
}