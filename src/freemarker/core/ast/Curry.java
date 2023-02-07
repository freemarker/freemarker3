package freemarker.core.ast;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import freemarker.core.Environment;
import freemarker.core.builtins.ExpressionEvaluatingBuiltIn;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateTransformModel;

/**
 * Implements the <tt>?curry</tt> built-in. Currying takes a macro, a function,
 * a method, or a transform and a concrete argument list (either named or 
 * positional), and returns a new macro/function/method/transform that has the 
 * parameters specified in the argument list defaulted to values specified in
 * the same argument list. I.e.
 * <pre>
 * [#macro label text color]
 * <font color="${color}>${text?html}</font>
 * [/#macro]
 * 
 * [#assign yellowLabel = label?curry(color="#ffff00")/]
 * 
 * [@yellowLabel text="This label will print in yellow"/]
 * </pre>
 * @author Attila Szegedi
 * @version $Id: $
 */
@SuppressWarnings("deprecation")
public class Curry extends ExpressionEvaluatingBuiltIn
{
	public TemplateModel get(Environment env, BuiltInExpression expression,
	        TemplateModel model)
	{
            if(model instanceof Macro) {
                return new MacroCurry((Macro)model);
            }
            return new TransformAndMethodCurry(model);
    }
    
    /**
     * Subclasses are recognized by {@link MethodCall} and treated specially to
     * implement currying instead of method invocation. Thus, f(x=y) is 
     * function invocation, while f?curry(x=y) is currying. The purpose of this
     * is that currying with all parameters specified is distinguished from 
     * method invocation (we must distinguish as FM can't use nonstrict 
     * invocation order, since functions can have side effects and may depend
     * on values other than their parameters). Also, this allows the currying 
     * operators to avoid having to implement TemplateMethodModelEx, and can 
     * thus work on Expression and ArgsList objects instead of on 
     * TemplateModels.
     * @author Attila Szegedi
     * @version $Id: $
     */
    static abstract class Operator implements TemplateModel {
        abstract TemplateModel curry(ArgsList arguments, Environment env);
    };
    
    /**
     * A generator for default value expressions specified in currying 
     * expressions. We have two implementations - one for named arglists and
     * one for positional arglists.
     * @author Attila Szegedi
     * @version $Id: $
     */
    private abstract static class DefaultValueGenerator
    {
        /**
         * Get the expression for either the next argument (positional), or the
         * named argument (named).
         * @param name the name of the next argument
         * @return the expression specifying the default value for the argument
         */
        abstract Expression getExpression(String name);
        
        /**
         * If possible, return the collection of specified arguments names that
         * is not in the given collection of acceptable arguments names
         * @param names the acceptable argument names
         * @return the collection of non-matching names, eventually empty.
         */
        abstract Collection<String> getBogusArgs(Collection<String> names);
    }

    private static class NamedDefaultValueGenerator extends DefaultValueGenerator
    {
        private final Map<String, Expression> args;
        
        NamedDefaultValueGenerator(NamedArgsList arguments) {
            args = arguments.getArgs();
        }
        
        @Override
        Expression getExpression(String name) {
            return args.get(name);
        }
        
        @Override
        Collection<String> getBogusArgs(Collection<String> names)
        {
            Collection<String> bogus = null;
            // Check for typos
            for(String argName : args.keySet()) {
                if(!names.contains(argName)) {
                    if(bogus == null) {
                        bogus = new ArrayList<String>();
                    }
                    bogus.add(argName);
                }
            }
            return bogus == null ? Collections.EMPTY_SET : bogus;
        }
    }
    
    private static class PositionalDefaultValueGenerator extends DefaultValueGenerator
    {
        private final Iterator<Expression> iter;
        
        public PositionalDefaultValueGenerator(PositionalArgsList arguments) {
            iter = arguments.getArgs().iterator();
        }
        
        @Override
        Expression getExpression(String name) {
            return iter.hasNext() ? iter.next() : null;
        }
        
        @Override
        Collection<String> getBogusArgs(Collection<String> names) {
            return Collections.emptySet();
        }
    }
    
    private static void curryExplictParamList(ArgsList arguments, 
            Environment env, ParameterList originalParams, 
            ParameterList curriedParams, String curriedType, String curriedName) 
    {
        DefaultValueGenerator gen = arguments instanceof NamedArgsList 
            ? new NamedDefaultValueGenerator((NamedArgsList)arguments)
            : new PositionalDefaultValueGenerator((PositionalArgsList)arguments);
        Collection<String> names = originalParams.getParamNames(); 
        Collection<String> bogus = gen.getBogusArgs(names); 
        if(!bogus.isEmpty()) {
            throw new TemplateModelException(curriedType + " " + 
                    curriedName + " has no argument(s) named " + bogus);
        }
        for (String paramName : names) {
            Expression defaultValue = gen.getExpression(paramName);
            if(defaultValue == null) {
                defaultValue = originalParams.getDefaultExpression(paramName);
            }
            if(defaultValue == null) {
                curriedParams.addParam(paramName);
            } else {
                curriedParams.addParam(paramName, new ModelLiteral(defaultValue, env));
            }
        }
        curriedParams.copyLocationFrom(originalParams);
    } 

    private static class MacroCurry extends Operator
    {
        private final Macro baseMacro;
        
        MacroCurry(Macro baseMacro) {
            this.baseMacro = baseMacro;
        }
        
        @Override
        public TemplateModel curry(ArgsList arguments, Environment env) {
            ParameterList originalParams = baseMacro.getParams();
            ParameterList curriedParams = new ParameterList();
            curriedParams.setCatchAll(originalParams.getCatchAll());
            curryExplictParamList(arguments, env, originalParams, curriedParams, "Macro", baseMacro.getName());
            return baseMacro.createCurriedMacro(curriedParams, env);
        } 
    }

    private static class TransformAndMethodCurry extends Operator
    {
        private final TemplateModel base;
        private final ParameterList originalParams;
        
        TransformAndMethodCurry(TemplateModel base) {
            if(base instanceof Curried) {
                Curried ct = (Curried)base;
                this.base = ct.getBase();
                this.originalParams = ct.getParameterList();
            } else {
                this.base = base;
                this.originalParams = ArgsList.getParameterList(base);
            }
        }
        
        @Override
        public TemplateModel curry(ArgsList arguments, Environment env) {
            ParameterList curriedParams = new ParameterList();
            boolean hasOriginalParams = originalParams != null;
            if(!hasOriginalParams || originalParams.isCurryGenerated()) {
                curriedParams.setCurryGenerated(true);
                if(arguments instanceof NamedArgsList) {
                    Map<String, Expression> args = ((NamedArgsList)arguments).getArgs();
                    Set<String> names = new HashSet<String>(args.keySet());
                    if(hasOriginalParams) {
                        names.addAll(originalParams.getParamNames());
                    }
                    for(String name : names) {
                        Expression value = args.get(name);
                        if(value == null && hasOriginalParams) {
                            value = originalParams.getDefaultExpression(name);
                        }
                        assert value != null;
                        curriedParams.addParam(name, new ModelLiteral(value, env));
                    }
                } else {
                    throw new TemplateException(
                            "Can't use positional arguments to curry an instance of "
                            + base.getClass().getName() + 
                            " as it has no @Parameters annotation", env);
                }
            } else {
                curriedParams.setCatchAll(originalParams.getCatchAll());
                curryExplictParamList(arguments, env, originalParams, 
                        curriedParams, "Transform", base.getClass().getName());
            }
            return Curried.instantiate(base, curriedParams);
        }
    }

    abstract static class Curried {
        private final ParameterList parameterList;
        
        Curried(ParameterList parameterList) {
            this.parameterList = parameterList;
        }
        
        ParameterList getParameterList() {
            return parameterList;
        }
        
        abstract TemplateModel getBase();
        
        static TemplateModel instantiate(TemplateModel base, ParameterList parameterList) 
        {
            if(base instanceof TemplateDirectiveModel) {
                return new CurriedDirective((TemplateDirectiveModel)base, parameterList);
            }
            if(base instanceof TemplateTransformModel) {
                return new CurriedTransform((TemplateTransformModel)base, parameterList);
            }
            if(base instanceof TemplateMethodModelEx) {
                return new CurriedMethodEx((TemplateMethodModelEx)base, parameterList);
            }
            if(base instanceof TemplateMethodModel) {
                return new CurriedMethod((TemplateMethodModel)base, parameterList);
            }
            throw new TemplateException("curry can only be applied to a macro, " +
                    "to a transform, or to a method. You tried to apply it to an " + 
                    "instance of " + base.getClass().getName() + 
                    " which is neither of the above", Environment.getCurrentEnvironment());
        }
    }
    
    static class CurriedTransform extends Curried implements TemplateTransformModel {
        private final TemplateTransformModel base;
        
        CurriedTransform(TemplateTransformModel base, ParameterList parameterList) {
            super(parameterList);
            this.base = base;
        }
        public Writer getWriter(Writer out, Map<String, TemplateModel> args) throws TemplateModelException, IOException
        {
            return base.getWriter(out, args);
        }
        
        @Override
        TemplateModel getBase()
        {
            return base;
        }
    }
    
    static class CurriedDirective extends Curried implements TemplateDirectiveModel {
        private final TemplateDirectiveModel base;

        CurriedDirective(TemplateDirectiveModel base, ParameterList parameterList) {
            super(parameterList);
            this.base = base;
        }
        
        public void execute(Environment env, Map<String, TemplateModel> params,
                TemplateModel[] loopVars, TemplateDirectiveBody body)
                throws IOException
        {
            base.execute(env, params, loopVars, body);
        }
        
        @Override
        TemplateModel getBase()
        {
            return base;
        }
    }
    
    static class CurriedMethod extends Curried implements TemplateMethodModel {
        private final TemplateMethodModel base;

        CurriedMethod(TemplateMethodModel base, ParameterList parameterList) {
            super(parameterList);
            this.base = base;
        }
        
        public Object exec(List arguments) throws TemplateModelException
        {
            return base.exec(arguments);
        }
        
        @Override
        TemplateModel getBase()
        {
            return base;
        }
    }

    static class CurriedMethodEx extends CurriedMethod implements TemplateMethodModelEx {
        CurriedMethodEx(TemplateMethodModelEx base, ParameterList parameterList) {
            super(base, parameterList);
        }
    }
    
    private static class ModelLiteral extends Expression {
        private final Expression underlying;
        
        public ModelLiteral(Expression underlying, Environment env) {
            this.constantValue = underlying.getAsTemplateModel(env);
            this.underlying = underlying;
            copyLocationFrom(underlying);
        }

        @Override
        TemplateModel _getAsTemplateModel(Environment env) {
            throw new UnsupportedOperationException();
        }

        @Override
        Expression _deepClone(String name, Expression subst) {
            throw new UnsupportedOperationException();
        }

        @Override boolean isLiteral() {
            return true;
        }
        
        @Override
        public String toString() {
            return underlying.toString();
        }
    }
}