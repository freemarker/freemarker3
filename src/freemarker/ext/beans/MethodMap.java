package freemarker.ext.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

class MethodMap<T extends Member>
{
    private final String name;
    private final BeansWrapper wrapper;
    private final OverloadedMethod<T> fixArgMethod = new OverloadedFixArgMethod<T>();
    private OverloadedMethod<T> varArgMethod;
    
    MethodMap(String name, BeansWrapper wrapper) {
        this.name = name;
        this.wrapper = wrapper;
    }
    
    BeansWrapper getWrapper() {
        return wrapper;
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
    
    MemberAndArguments<T> getMemberAndArguments(List<TemplateModel> arguments) 
    {
        Object memberAndArguments = fixArgMethod.getMemberAndArguments(arguments, wrapper);
        if(memberAndArguments == OverloadedMethod.NO_SUCH_METHOD) {
            if(varArgMethod != null) {
                memberAndArguments = varArgMethod.getMemberAndArguments(arguments, wrapper);
            }
            if(memberAndArguments == OverloadedMethod.NO_SUCH_METHOD) {
                throw new TemplateModelException("No signature of method " + 
                        name + " matches the arguments");
            }
        }
        if(memberAndArguments == OverloadedMethod.AMBIGUOUS_METHOD) {
            throw new TemplateModelException("Multiple signatures of method " + 
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
