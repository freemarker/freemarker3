package freemarker.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;

public interface BuiltIn {
    Object get(Environment env, BuiltInExpression caller);
}
