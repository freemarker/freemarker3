package freemarker.core.ast;

import java.util.*;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.template.utility.UndeclaredThrowableException;
import freemarker.core.parser.ast.Expression;
import freemarker.core.parser.ast.TemplateNode;

public class PositionalArgsList extends ArgsList {

    List<Expression> args = new ArrayList<Expression>();

    public List<Expression> getArgs() {
        return args;
    }

    public void addArg(Expression exp) {
        args.add(exp);
        exp.setParent(this);
    }

    Map<String, Object> getParameterMap(Object target, Environment env)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        ParameterList annotatedParameterList = getParameterList(target);
        if (annotatedParameterList == null) {
            if (!args.isEmpty()) {
                throw new TemplateModelException("Cannot pass positional arguments to this TemplateTransformModel");
            }
        }
        else {
            result = annotatedParameterList.getParameterMap(this, env, false);
        }
        return result;
    }

    public List getParameterSequence(Object target, Environment env) {
        ParameterList annotatedParameterList = getParameterList(target);
        if (annotatedParameterList == null) {
            return target instanceof TemplateMethodModelEx ? 
                    (target instanceof LazilyEvaluatableArguments ? 
                            getLazyModelList(env) :
                                getModelList(env)) :
                                    (target instanceof LazilyEvaluatableArguments ? 
                                            getLazyValueList(env) :
                                                getValueList(env));
        }
        // TODO: Attila's lazy evaluation machinery here as well, I guess FWIW...
        List<Object> result = annotatedParameterList.getParameterSequence(this, env);
        if ((target instanceof TemplateMethodModel) && !(target instanceof TemplateMethodModelEx)) {
            List<String> strings = new ArrayList<String>();
            for(int i = 0; i < result.size(); ++i) {
                Object value = result.get(i);
                Expression exp;
                if(i < args.size()) {
                    exp = args.get(i);
                }
                else {
                    exp = annotatedParameterList.getDefaultExpression(i);
                }
                strings.add(Expression.getStringValue(value, exp, env));
            }
            return strings;
        }
        return result;
    }

    public int size() {
        return args == null ? 0 : args.size();
    }

    public String getStartLocation() {
        if (args != null && args.size() >0) {
            return args.get(0).getStartLocation();
        }
        else {
            return ""; // REVISIT 
        }
    }

    public Object getValueAt(int i, Environment env) {
        Expression exp = args.get(i);
        Object value = exp.evaluate(env);
        TemplateNode.assertIsDefined(value, exp, env);
        return value;
    }


    private static abstract class ExpressionTransformator {
        abstract Object transform(Expression exp, Environment env) 
        throws TemplateException;
    }

    private static class ToValueTransformator extends ExpressionTransformator {
        static final ExpressionTransformator INSTANCE = new ToValueTransformator(); 

        Object transform(Expression exp, Environment env) 
        {
            return exp.getStringValue(env);
        }
    }

    private static class ToModelTransformator extends ExpressionTransformator {
        static final ExpressionTransformator INSTANCE = new ToModelTransformator(); 

        Object transform(Expression exp, Environment env) 
        {
            return exp.evaluate(env);
        }
    }

    private List getList(Environment env, ExpressionTransformator transformator)
    {
        int size = args.size();
        switch(size) {
            case 0: {
                return Collections.EMPTY_LIST;
            }
            case 1: {
                return Collections.singletonList(transformator.transform(args.get(0), env));
            }
            default: {
                List result = new ArrayList(args.size());
                for (Expression arg : args) {
                    result.add(transformator.transform(arg, env));
                }
                return result;
            }
        }
    }

    /**
     * For the benefit of method calls, return the list of arguments as a list
     * of values.
     */
    List getValueList(Environment env) {
        return getList(env, ToValueTransformator.INSTANCE);
    }

    /**
     * For the benefit of extended method calls, return the list of arguments as
     * a list of template models.
     */
    List getModelList(Environment env) {
        return getList(env, ToModelTransformator.INSTANCE);
    }

    /**
     * For the benefit of lazily evaluatable method calls, return the list of 
     * arguments as a list of lazily evaluated values.
     */
    List getLazyValueList(Environment env) {
        return new LazyEvaluationList(ToValueTransformator.INSTANCE, env);
    }

    /**
     * For the benefit of lazily evaluatable extended method calls, return the 
     * list of arguments as a list of lazily evaluated models.
     */
    List getLazyModelList(Environment env) {
        return new LazyEvaluationList(ToModelTransformator.INSTANCE, env);
    }

    private class LazyEvaluationList extends AbstractList {
        private final ExpressionTransformator transformator;
        private final Environment env;
        private final Object[] resolvedValues;
        private final boolean[] resolved;

        LazyEvaluationList(ExpressionTransformator transformator, Environment env) {
            this.transformator = transformator;
            this.env = env;
            int size = args.size();
            resolvedValues = new Object[size];
            resolved = new boolean[size];

        }

        public int size() {
            return resolvedValues.length;
        }

        public Object get(int index) {
            if(resolved[index]) {
                return resolvedValues[index];
            }
            Expression exp = args.get(index);
            try {
                Object obj = transformator.transform(exp, env); 
                resolved[index] = true;
                return resolvedValues[index] = obj;
            }
            catch(TemplateException e) {
                throw new UndeclaredThrowableException(e);
            }
        }
    }


    public ArgsList deepClone(String name, Expression subst) {
        PositionalArgsList result = new PositionalArgsList();
        for (Expression arg : args) {
            result.addArg(arg.deepClone(name, subst));
        }
        return result;
    }
}
