package freemarker.core.ast;

import freemarker.core.parser.ast.Expression;
import freemarker.core.Environment;

public abstract class BooleanExpression extends Expression {

    public Object evaluate(Environment env)
    {
        return isTrue(env);
    }
}
