package freemarker.ext.beans;

import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import freemarker.template.WrappedVariable;
import static freemarker.ext.beans.ObjectWrapper.unwrap;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class OverloadedVarArgMethod<T extends Member> extends OverloadedMethod<T>
{
    private static final ConcurrentMap<ArgumentPacker, ArgumentPacker> canoncialArgPackers = 
	new ConcurrentHashMap<ArgumentPacker, ArgumentPacker>();
    
    private final Map<T, ArgumentPacker> argPackers = new HashMap<T, ArgumentPacker>();
    
    private static class ArgumentPacker {
        private final int argCount;
        private final Class<?> varArgType;
        
        ArgumentPacker(Class<?>[] argTypes) {
            argCount = argTypes.length;
            varArgType = argTypes[argCount - 1].getComponentType(); 
        }
        
        Object[] packArgs(Object[] args, List<WrappedVariable> modelArgs) 
        {
            final int actualArgCount = args.length;
            final int fixArgCount = argCount - 1;
            if(args.length != argCount) {
                Object[] newargs = new Object[argCount];
                System.arraycopy(args, 0, newargs, 0, fixArgCount);
                Object array = Array.newInstance(varArgType, actualArgCount - fixArgCount);
                for(int i = fixArgCount; i < actualArgCount; ++i) {
                    Object val = unwrap(modelArgs.get(i));
                    Array.set(array, i - fixArgCount, val);
                }
                newargs[fixArgCount] = array;
                return newargs;
            }
            else {
                Object val = unwrap(modelArgs.get(fixArgCount));
                Object array = Array.newInstance(varArgType, 1);
                Array.set(array, 0, val);
                args[fixArgCount] = array;
                return args;
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ArgumentPacker) {
        	ArgumentPacker p = (ArgumentPacker)obj;
        	return argCount == p.argCount && varArgType == p.varArgType;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return argCount ^ varArgType.hashCode();
        }
    }

    void onAddSignature(T member, Class<?>[] argTypes) {
	ArgumentPacker argPacker = new ArgumentPacker(argTypes);
	ArgumentPacker canonical = canoncialArgPackers.putIfAbsent(argPacker, 
		argPacker);
        argPackers.put(member, canonical != null ? canonical : argPacker);
        componentizeLastType(argTypes);
    }

    void updateSignature(int l) {
	Class<?>[][] marshalTypes = getMarshalTypes();
	Class<?>[] newTypes = marshalTypes[l];
        // First vararg marshal type spec with less parameters than the 
        // current spec influences the types of the current marshal spec.
        for(int i = l; i-->0;) {
            Class<?>[] previousTypes = marshalTypes[i];
            if(previousTypes != null) {
                varArgUpdate(newTypes, previousTypes);
                break;
            }
        }
        // Vararg marshal spec with exactly one parameter more than the current
        // spec influences the types of the current spec
        if(l + 1 < marshalTypes.length) {
            Class<?>[] oneLongerTypes = marshalTypes[l + 1];
            if(oneLongerTypes != null) {
                varArgUpdate(newTypes, oneLongerTypes);
            }
        }
    }
    
    void afterSignatureAdded(int l) {
	// Since this member is vararg, its types influence the types in all
        // type specs longer than itself.
	Class<?>[][] marshalTypes = getMarshalTypes();
        Class<?>[] newTypes = marshalTypes[l];
        for(int i = l + 1; i < marshalTypes.length; ++i) {
            Class<?>[] existingTypes = marshalTypes[i];
            if(existingTypes != null) {
                varArgUpdate(existingTypes, newTypes);
            }
        }
        // It also influences the types in the marshal spec that is exactly
        // one argument shorter (as vararg methods can be invoked with 0
        // variable arguments, that is, with k-1 cardinality).
        if(l > 0) {
            Class<?>[] oneShorterTypes = marshalTypes[l - 1];
            if(oneShorterTypes != null) {
                varArgUpdate(oneShorterTypes, newTypes);
            }
        }
    }
    
    private static void varArgUpdate(Class<?>[] modifiedTypes, Class<?>[] modifyingTypes) {
        final int dl = modifiedTypes.length;
        final int gl = modifyingTypes.length;
        int min = Math.min(gl, dl);
        for(int i = 0; i < min; ++i) {
            modifiedTypes[i] = OverloadedMethodUtilities.getMostSpecificCommonType(modifiedTypes[i], 
                    modifyingTypes[i]);
        }
        if(dl > gl) {
            Class<?> varArgType = modifyingTypes[gl - 1];
            for(int i = gl; i < dl; ++i) {
                modifiedTypes[i] = OverloadedMethodUtilities.getMostSpecificCommonType(modifiedTypes[i], 
                        varArgType);
            }
        }
    }
    
    private static void componentizeLastType(Class<?>[] types) {
        int l1 = types.length - 1;
        assert l1 >= 0;
        assert types[l1].isArray();
        types[l1] = types[l1].getComponentType();
    }
    
    Object getMemberAndArguments(List<WrappedVariable> arguments) 
    {
        if(arguments == null) {
            // null is treated as empty args
            arguments = Collections.emptyList();
        }
        int l = arguments.size();
	Class<?>[][] marshalTypes = getMarshalTypes();
        Object[] args = new Object[l];
        // Starting from args.length + 1 as we must try to match against a case
        // where all specified args are fixargs, and the vararg portion 
        // contains zero args
outer:  for(int j = Math.min(l + 1, marshalTypes.length - 1); j >= 0; --j) {
            Class<?>[] types = marshalTypes[j];
            if(types == null) {
                if(j == 0) {
                    return NO_SUCH_METHOD;
                }
                continue;
            }
            // Try to marshal the arguments
            Iterator<WrappedVariable> it = arguments.iterator();
            for(int i = 0; i < l; ++i) {
                Object dst = unwrap(it.next());
                if(dst != args[i]) {
                    args[i] = dst;
                }
            }
            break;
        }
        
        Object objMember = getMemberForArgs(args, true);
        if(objMember instanceof Member) {
            T member = (T)objMember;
            args = argPackers.get(member).packArgs(args, arguments);
            if(args == null) {
                return NO_SUCH_METHOD;
            }
            ObjectWrapper.coerceBigDecimals(getSignature(member), args);
            return new MemberAndArguments<T>(member, args);
        }
        return objMember; // either NOT_FOUND or AMBIGUOUS
    }
}