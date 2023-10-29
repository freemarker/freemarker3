package freemarker.core.evaluation;

import java.lang.reflect.Member;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static freemarker.core.evaluation.ObjectWrapper.unwrap;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class OverloadedFixArgMethod<T extends Member> extends OverloadedMethod<T>
{
    void onAddSignature(T member, Class<?>[] argTypes) {
    };
    
    void updateSignature(int l) {
    };
    
    void afterSignatureAdded(int l) {
    };

    Object getMemberAndArguments(List<Object> arguments) 
    {
        if(arguments == null) {
            // null is treated as empty args
            arguments = Collections.emptyList();
        }
        int l = arguments.size();
	Class<?>[][] marshalTypes = getMarshalTypes();
        if(marshalTypes.length <= l) {
            return NO_SUCH_METHOD;
        }
        Class<?>[] types = marshalTypes[l];
        if(types == null) {
            return NO_SUCH_METHOD;
        }
        assert types.length == l;
        // Marshal the arguments
        Object[] args = new Object[l];
        Iterator<Object> it = arguments.iterator();
        for(int i = 0; i < l; ++i) {
            args[i] = unwrap(it.next());
        }
        
        Object objMember = getMemberForArgs(args, false);
        if(objMember instanceof Member) {
            T member = (T)objMember;
            ObjectWrapper.coerceBigDecimals(getSignature(member), args);
            return new MemberAndArguments<T>(member, args);
        }
        return objMember; // either NO_SUCH_METHOD or AMBIGUOUS_METHOD
    }
}
