package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;

abstract public class BuiltIn {
    abstract public Object get(Environment env, BuiltInExpression caller);
}
