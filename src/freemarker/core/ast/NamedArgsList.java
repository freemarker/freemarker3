package freemarker.core.ast;

import java.util.*;

import freemarker.core.Environment;
import freemarker.core.parser.ParseException;
import freemarker.core.parser.ast.TemplateNode;
import freemarker.template.*;

public class NamedArgsList extends ArgsList {


    private LinkedHashMap<String,Expression> namedArgs = 
        new LinkedHashMap<String, Expression>();

    public void addNamedArg(String name, Expression exp) throws ParseException{
        if (namedArgs.containsKey(name)) throw new ParseException(
                "Error at: " + exp.getStartLocation() + "\nArgument " + name + " was already specified.");
        namedArgs.put(name, exp);
        exp.setParent(this);
    }

    public Map<String, Expression> getArgs() {
        return namedArgs;
    }

    public int size() {
        return namedArgs.size();
    }

    Map<String,Expression> getCopyOfMap() {
        return (Map<String,Expression>)namedArgs.clone();
    }

    Map<String, Object> getParameterMap(Object tm, Environment env) {
        Map<String, Object> result = null; 
        ParameterList annotatedParameterList = ArgsList.getParameterList(tm);
        if (annotatedParameterList == null) {
            result = new HashMap<String, Object>();
            for (String paramName : namedArgs.keySet()) {
                Expression exp = namedArgs.get(paramName);
                Object value = exp.evaluate(env);
                TemplateNode.assertIsDefined(value, exp, env);
                result.put(paramName, value);
            }
        }
        else {
            result = annotatedParameterList.getParameterMap(this, env);
        }
        return result;
    }

    public List getParameterSequence(Object target, Environment env) {
        ParameterList annotatedParameterList = getParameterList(target);
        if (annotatedParameterList == null) {
            String msg = "Error at: " + getStartLocation() 
            + "\nCannot invoke method " + target + " with a key=value parameter list because it is not annotated.";
            throw new TemplateException(msg, env);
        }
        List<Object> result = annotatedParameterList.getParameterSequence(this, env);
        if ((target instanceof TemplateMethodModel) && !(target instanceof TemplateMethodModelEx)) {
            List<String> strings = new ArrayList<String>();
            List<String> paramNames = annotatedParameterList.getParamNames();
            for(int i = 0; i < result.size(); ++i) {
                Object value = result.get(i);
                Expression exp;
                String paramName = paramNames.get(i);
                exp = namedArgs.get(paramName);
                if(exp == null) {
                    exp = annotatedParameterList.getDefaultExpression(paramName);
                }
                strings.add(Expression.getStringValue(value, exp, env));
            }
            return strings;
        }
        return result;
    }


    public String getStartLocation() {
        for (Expression exp : namedArgs.values()) {
            return exp.getStartLocation();
        }
        return "";
    }

    public ArgsList deepClone(String name, Expression subst) {
        NamedArgsList result = new NamedArgsList();
        for (Map.Entry<String, Expression> entry : namedArgs.entrySet()) {
            try {
                result.addNamedArg(entry.getKey(), entry.getValue());
            } catch (ParseException pe) {} // This can't happen anyway, since we already checked for repeats
        }
        return result;
    }
}
