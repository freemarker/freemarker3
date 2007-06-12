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
 * <p><font color="red">Experimental: no backward compatibility guarantees</font>;
 * any feedback is highly welcome!</p>
 * 
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
            args[i] = wrapper.unwrap((TemplateModel)args[i], Scriptable.class);
        }
        return wrapper.wrap(((Function)getScriptable()).call(cx, 
                ScriptableObject.getTopLevelScope(fnThis), fnThis, args));
    }
}
