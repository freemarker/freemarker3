package freemarker.core.ast;

import java.io.*;
import java.util.*;

import freemarker.core.Environment;
import freemarker.template.*;

/**
 * A template element that contains a nested block
 * that is transformed according to an instance of T
 * TemplateTransformModel
 */
public class TransformBlock extends TemplateElement {

    private Expression transformExpression;
    Map<String, Expression> namedArgs;

    /**
     * Creates new TransformBlock, with a given transformation
     */
    public TransformBlock(Expression transformExpression, 
                   Map<String,Expression> namedArgs,
                   TemplateElement nestedBlock) {
        this.transformExpression = transformExpression;
        this.namedArgs = namedArgs;
        this.setNestedBlock(nestedBlock);
    }
    
    public Expression getTransformExpression() {
    	return transformExpression;
    }
    
    public Map getArgs() {
    	return namedArgs == null ? 
    			Collections.EMPTY_MAP :
    			Collections.unmodifiableMap(namedArgs);
    }

    public void execute(Environment env) throws TemplateException, IOException
    {
        TemplateTransformModel ttm = env.getTransform(transformExpression);
        if (ttm != null) {
            Map<String,Object> args = new HashMap<String,Object>();
            if (namedArgs != null && !namedArgs.isEmpty()) {
                for (Iterator it = namedArgs.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry) it.next();
                    String key = (String) entry.getKey();
                    Expression valueExp = (Expression) entry.getValue();
                    Object value = valueExp.getAsTemplateModel(env);
                    args.put(key, value);
                }
            } 
            env.render(getNestedBlock(), ttm, args);
        }
        else {
            Object tm = transformExpression.getAsTemplateModel((Environment)env);
            throw invalidTypeException(tm, transformExpression, env, "transform");
        }
    }

    public String getDescription() {
        return "transform " + transformExpression;
    }
}
