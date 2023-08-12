package freemarker.core.ast;

import java.util.*;
import freemarker.core.Environment;

/**
 * The abstract base class of both {@link NamedArgsList} and {@link PositionalArgsList}
 * @author Attila Szegedi
 */

public class EmptyArgsList extends ArgsList {

    @Override
    public ArgsList deepClone(String name, Expression subst) {
        return this;
    }

    @Override
    Map<String, Object> getParameterMap(Object tm, Environment env) {
        ParameterList annotatedParameterList = ArgsList.getParameterList(tm);
        if (annotatedParameterList == null) {
            return new HashMap<String, Object>();
        }
        else {
            return annotatedParameterList.getParameterMapForEmptyArgs(env);
        }
    }

    @Override
    public List getParameterSequence(Object target, Environment env) {
        throw new UnsupportedOperationException();
    }

    //@Override
    public int size() {return 0;}
}