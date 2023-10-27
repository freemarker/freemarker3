package freemarker.ext.beans;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import freemarker.template.WrappedVariable;
import freemarker.template.EvaluationException;
import static freemarker.ext.beans.ObjectWrapper.unwrap;

/**
 * This class is used for constructors and as a base for non-overloaded methods
 * @author Attila Szegedi
 * @version $Id: $
 * @param <T>
 */
class SimpleMemberModel<T extends Member>
{
    private final T member;
    private final Class<?>[] argTypes;
    
    protected SimpleMemberModel(T member, Class<?>[] argTypes)
    {
        this.member = member;
        this.argTypes = argTypes;
    }
    
    Object[] unwrapArguments(List<WrappedVariable> arguments) {
        if(arguments == null) {
            arguments = Collections.emptyList();
        }
        boolean varArg = member instanceof Method ? ((Method)member).isVarArgs() : ((Constructor<?>)member).isVarArgs();
        int typeLen = argTypes.length;
        if(varArg) {
            if(typeLen - 1 > arguments.size()) {
                throw new EvaluationException("Method " + member + 
                        " takes at least " + (typeLen - 1) + 
                        " arguments");
            }
        }
        else if(typeLen != arguments.size()) {
            throw new EvaluationException("Method " + member + 
                    " takes exactly " + typeLen + " arguments");
        }
        Object[] args = null;
        if (arguments != null) {
            args = arguments.toArray(new Object[arguments.size()]);
            for (int i = 0; i< args.length; i++) {
                args[i] = unwrap(args[i]);
            }
        }
        if(args != null) {
            ObjectWrapper.coerceBigDecimals(argTypes, args);
            if(varArg && shouldPackVarArgs(args)) {
                args = packVarArgs(args, argTypes);
            }
        }
        return args;
    }

    private boolean shouldPackVarArgs(Object[] args) {
        int l = args.length;
        if(l == argTypes.length) {
            assert l > 0; // varArg methods must have at least one declared arg
            Object lastArg = args[l - 1];
            if(lastArg == null || argTypes[l - 1].getComponentType().isInstance(lastArg)) {
                return false;
            }
        }
        return true;
    }
    
    static Object[] packVarArgs(Object[] args, Class[] argTypes)
    {
        int argsLen = args.length;
        int typeLen = argTypes.length;
        int fixArgsLen = typeLen - 1;
        Object varArray = Array.newInstance(argTypes[fixArgsLen], 
                argsLen - fixArgsLen);
        for (int i = fixArgsLen; i < argsLen; i++) {
            Array.set(varArray, i - fixArgsLen, args[i]);
        }
        if(argsLen != typeLen) {
            Object[] newArgs = new Object[typeLen];
            System.arraycopy(args, 0, newArgs, 0, fixArgsLen);
            args = newArgs;
        }
        args[fixArgsLen] = varArray;
        return args;
    }

    protected T getMember() {
        return member;
    }
}