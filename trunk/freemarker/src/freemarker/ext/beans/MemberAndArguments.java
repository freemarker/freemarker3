package freemarker.ext.beans;

import java.lang.reflect.Member;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class MemberAndArguments<T extends Member> {
    private final T member;
    private final Object[] args;
    
    MemberAndArguments(T member, Object[] args) {
        this.member = member;
        this.args = args;
    }
    
    Object[] getArgs() {
        return args;
    }
    
    public T getMember() {
        return member;
    }
}
