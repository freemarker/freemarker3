package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.parser.ast.BuiltInExpression;

abstract public class BuiltIn {
    abstract public Object get(Environment env, BuiltInExpression caller);
}
