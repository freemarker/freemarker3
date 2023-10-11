package freemarker.core.ast;

import java.util.*;
import freemarker.core.parser.ast.Expression;
import freemarker.core.Environment;
import freemarker.core.parser.ast.ArgsList;

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
    public Map<String, Object> getParameterMap(Object tm, Environment env) {
        ParameterList annotatedParameterList = ArgsList.getParameterList(tm);
        if (annotatedParameterList == null) {
            return new HashMap<String, Object>();
        }
        else {
            return annotatedParameterList.getParameterMapForEmptyArgs(env);
        }
    }

    @Override
    public List<Object> getParameterSequence(Object target, Environment env) {
        throw new UnsupportedOperationException();
    }
}