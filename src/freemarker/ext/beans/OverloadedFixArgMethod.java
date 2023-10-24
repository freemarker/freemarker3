package freemarker.ext.beans;

import java.lang.reflect.Member;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import freemarker.template.TemplateModel;

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

    Object getMemberAndArguments(List<TemplateModel> arguments, ObjectWrapper w) 
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
        Iterator<TemplateModel> it = arguments.iterator();
        for(int i = 0; i < l; ++i) {
            Object obj = w.unwrap(it.next(), types[i]);
            if(obj == ObjectWrapper.CAN_NOT_UNWRAP) {
                return NO_SUCH_METHOD;
            }
            args[i] = obj;
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
