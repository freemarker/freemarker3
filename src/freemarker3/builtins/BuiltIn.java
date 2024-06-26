package freemarker3.builtins;

import freemarker3.core.Environment;
import freemarker3.core.nodes.generated.BuiltInExpression;

public interface BuiltIn {
    Object get(Environment env, BuiltInExpression caller);
}
