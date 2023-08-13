package freemarker.template.utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.Constants;
;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

import static freemarker.ext.beans.ObjectWrapper.*;

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
     *   <li>If the object implements {@link TemplateDateModel}, then the result
     *       of {@link TemplateDateModel#getAsDate()} is returned.
     *   <li>If the object implements {@link TemplateSequenceModel} or
     *       {@link Iterable}, then a <code>java.util.ArrayList</code> is
     *       constructed from the subvariables, and each subvariable is unwrapped with
     *       the rules described here (recursive unwrapping).
     *   <li>If the object implements {@link TemplateHashModelEx}, then a
     *       <code>java.util.HashMap</code> is constructed from the subvariables, and each
     *       subvariable is unwrapped with the rules described here (recursive unwrapping).
     *   <li>If the object is {@link Constants#JAVA_NULL}, then null is returned.
     *   <li>Throw a <code>TemplateModelException</code>, because it doesn't know how to
     *       unwrap the object.
     * </ol>
     */
    public static Object unwrap(Object model) {
        return unwrap(model, false);
    }

    /**
     * Same as {@link #unwrap(TemplateModel)}, but it doesn't throw exception 
     * if it doesn't know how to unwrap the model, but rather returns it as-is.
     * @since 2.3.14
     */
    public static Object permissiveUnwrap(Object model) {
        return unwrap(model, true);
    }
    
    /**
     * @deprecated the name of this method is mistyped. Use 
     * {@link #permissiveUnwrap(Object)} instead.
     */
    public static Object premissiveUnwrap(TemplateModel model) {
        return unwrap(model, true);
    }
    
    private static Object unwrap(Object model, boolean permissive) {
        if(model instanceof AdapterTemplateModel) {
            return ((AdapterTemplateModel)model).getAdaptedObject(OBJECT_CLASS);
        }
        if (isNumber(model)) {
            return asNumber(model);
        }
        if (isString(model)) {
            return asString(model);
        }
        if(model instanceof TemplateDateModel) {
            return ((TemplateDateModel)model).getAsDate();
        }
        if (isBoolean(model)) {
            return asBoolean(model);
        }
        if(model instanceof TemplateSequenceModel) {
            TemplateSequenceModel seq = (TemplateSequenceModel)model;
            ArrayList<Object> list = new ArrayList<Object>(seq.size());
            for(int i = 0; i < seq.size(); ++i) {
                list.add(unwrap(seq.get(i), permissive));
            }
            return list;
        }
        if(model instanceof Iterable) {
            Iterable coll = (Iterable)model;
            ArrayList<Object> list = new ArrayList<Object>();
            Iterator<Object> it = coll.iterator();            
            while(it.hasNext()) {
                list.add(unwrap(it.next(), permissive));
            }
            return list;
        }
        if(model instanceof TemplateHashModelEx) {
            TemplateHashModelEx hash = (TemplateHashModelEx)model;
            Map map = new LinkedHashMap();
            Iterator<?> keys = hash.keys().iterator();
            while(keys.hasNext()) {
                String key = (String)unwrap(keys.next(), permissive); 
                map.put(key, unwrap(hash.get(key), permissive));
            }
            return map;
        }
        if(model == Constants.JAVA_NULL) {
            return null;
        }
        if (permissive) {
            return model;
        }
        throw new TemplateModelException("Cannot deep-unwrap model of type " + model.getClass().getName());
    }
}