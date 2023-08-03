package freemarker.template.utility;

import java.util.List;
import freemarker.ext.beans.ObjectWrapper;
import freemarker.template.*;

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
    public Object exec(List args) {
        if (args.isEmpty()) {
            throw new TemplateModelException("This method must have at least one argument, the name of the class to instantiate.");
        }
        String classname = args.get(0).toString();
        Class cl = null;
        try {
            cl = Class.forName(classname);
        }
        catch (Exception e) {
            throw new TemplateModelException(e.getMessage());
        }
        ObjectWrapper bw = ObjectWrapper.getDefaultInstance();
        Object obj = bw.newInstance(cl, args.subList(1, args.size()));
        return bw.wrap(obj);
    }
}
