package freemarker.core.variables;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

class MethodMap<T extends Member>
{
    private final String name;
    private final OverloadedMethod<T> fixArgMethod = new OverloadedFixArgMethod<T>();
    private OverloadedMethod<T> varArgMethod;
    
    MethodMap(String name) {
        this.name = name;
    }
    
    void addMember(T member) {
        fixArgMethod.addMember(member);
        if(isVarArgs(member)) {
            if(varArgMethod == null) {
                varArgMethod = new OverloadedVarArgMethod<T>();
            }
            varArgMethod.addMember(member);
        }
    }
    
    MemberAndArguments<T> getMemberAndArguments(List<Object> arguments) 
    {
        Object memberAndArguments = fixArgMethod.getMemberAndArguments(arguments);
        if(memberAndArguments == OverloadedMethod.NO_SUCH_METHOD) {
            if(varArgMethod != null) {
                memberAndArguments = varArgMethod.getMemberAndArguments(arguments);
            }
            if(memberAndArguments == OverloadedMethod.NO_SUCH_METHOD) {
                throw new EvaluationException("No signature of method " + 
                        name + " matches the arguments");
            }
        }
        if(memberAndArguments == OverloadedMethod.AMBIGUOUS_METHOD) {
            throw new EvaluationException("Multiple signatures of method " + 
                    name + " match the arguments");
        }
        return (MemberAndArguments<T>)memberAndArguments;
    }
    
    private static boolean isVarArgs(Member member) {
        if(member instanceof Method) { 
            return ((Method)member).isVarArgs();
        }
        if(member instanceof Constructor) {
            return ((Constructor)member).isVarArgs();
        }
        throw new AssertionError();
    }
}
