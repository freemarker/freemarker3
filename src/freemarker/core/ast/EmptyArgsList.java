package freemarker.core.ast;

import java.util.*;
import freemarker.core.Environment;
import freemarker.template.*;

/**
 * The abstract base class of both {@link NamedArgsList} and {@link PositionalArgsList}
 * @author Attila Szegedi
 */

public class EmptyArgsList extends ArgsList {

    @Override
    void addOOParamArg(OOParamElement param) {
        throw new AssertionError();
    }

    @Override
    ArgsList deepClone(String name, Expression subst) {
        return this;
    }

    @Override
    Map<String, TemplateModel> getParameterMap(TemplateModel tm, Environment env) {
        ParameterList annotatedParameterList = ArgsList.getParameterList(tm);
        if (annotatedParameterList == null) {
            return new HashMap<String, TemplateModel>();
        }
        else {
            return annotatedParameterList.getParameterMapForEmptyArgs(env);
        }
    }

    @Override
    List getParameterSequence(TemplateModel target, Environment env) {
        throw new UnsupportedOperationException();
    }

    @Override
    int size() {return 0;}
}