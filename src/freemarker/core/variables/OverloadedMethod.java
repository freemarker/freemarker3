package freemarker.core.variables;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Attila Szegedi
 * @version $Id: $
 * @param <T>
 */
abstract class OverloadedMethod<T extends Member> {
    static final Object NO_SUCH_METHOD = new Object();
    static final Object AMBIGUOUS_METHOD = new Object();

    private Class<?>[][] marshalTypes;
    private final Map<ClassString<T>, Object> selectorCache = new ConcurrentHashMap<>();
    private final List<T> members = new LinkedList<>();
    private final Map<T, Class<?>[]> signatures = new HashMap<>();

    void addMember(T member) {
        members.add(member);

        Class<?>[] argTypes = OverloadedMethodUtilities.getParameterTypes(member);
        int l = argTypes.length;
        onAddSignature(member, argTypes);
        if (marshalTypes == null) {
            marshalTypes = new Class[l + 1][];
            marshalTypes[l] = argTypes;
            updateSignature(l);
        } else if (marshalTypes.length <= l) {
            Class<?>[][] newMarshalTypes = new Class[l + 1][];
            System.arraycopy(marshalTypes, 0, newMarshalTypes, 0, marshalTypes.length);
            marshalTypes = newMarshalTypes;
            marshalTypes[l] = argTypes;
            updateSignature(l);
        } else {
            Class<?>[] oldTypes = marshalTypes[l];
            if (oldTypes == null) {
                marshalTypes[l] = argTypes;
            } else {
                for (int i = 0; i < oldTypes.length; ++i) {
                    oldTypes[i] = OverloadedMethodUtilities.getMostSpecificCommonType(oldTypes[i], argTypes[i]);
                }
            }
            updateSignature(l);
        }

        afterSignatureAdded(l);
    }

    Class<?>[] getSignature(T member) {
        Class<?>[] signature = signatures.get(member);
        if (signature == null) {
            signatures.put(member, signature = OverloadedMethodUtilities.getParameterTypes(member));
        }
        return signature;
    }

    Class<?>[][] getMarshalTypes() {
        return marshalTypes;
    }

    Object getMemberForArgs(Object[] args, boolean varArg) {
        ClassString<T> argTypes = new ClassString<T>(args);
        Object objMember = selectorCache.get(argTypes);
        if (objMember == null) {
            objMember = argTypes.getMostSpecific(members, varArg);
            selectorCache.put(argTypes, objMember);
        }
        return objMember;
    }

    abstract void onAddSignature(T member, Class<?>[] argTypes);

    abstract void updateSignature(int l);

    abstract void afterSignatureAdded(int l);

    abstract Object getMemberAndArguments(List<Object> arguments);
}
