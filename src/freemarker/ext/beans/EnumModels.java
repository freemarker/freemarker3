package freemarker.ext.beans;

import java.util.LinkedHashMap;
import java.util.Map;

import freemarker.template.TemplateModel;

/**
 * @author Attila Szegedi
 * @version $Id: EnumModels.java,v 1.1 2005/11/03 08:49:19 szegedia Exp $
 */
class EnumModels extends ClassBasedModelFactory {

    EnumModels(ObjectWrapper wrapper) {
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
